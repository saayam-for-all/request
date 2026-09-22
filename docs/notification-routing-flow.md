# Request notification flow: sentiment → matching → dispatch

How a help request becomes a set of volunteer notifications, and where each decision
is made. Companion to [`notification-runtime-setup.md`](notification-runtime-setup.md),
which covers the AWS wiring.

## The flow

```
Request created
      │
      ▼
┌─────────────────────┐
│ 1. Sentiment        │  SentimentAnalysisService.assess(request)
│    analysis         │
└─────────┬───────────┘
          │
    code == 0 (GOOD_REQUEST)? ──── no ──▶ log the code, stop.
          │                               No matching, no notification,
         yes                              no change to how the request is stored.
          │
          ▼
┌─────────────────────┐
│ 2. Volunteer        │  VolunteerMatchService.resolveMatchedVolunteerRecipients
│    matching         │
└─────────┬───────────┘
          │
    requestTypeId == 1 (IN_PERSON)?
          │
    ┌─────┴───────────────────────┐
   yes                            no  (REMOTE, UNSPECIFIED, or absent)
    │                              │
    ▼                              ▼
 proximity                      skill
 volunteer_locations            user_skills
 ST_DWithin around the          cat_id == request's
 requester, nearest first       help category
    │                              │
    └─────────────┬────────────────┘
                  │
          any volunteers? ──── no ──▶ log the no-match, stop (ba#40 FR-08).
                  │
                 yes
                  │
                  ▼
      ┌─────────────────────┐
      │ 3. Dispatch         │  one IN_APP recipient per volunteer,
      │                     │  plus EMAIL where an address is on file
      └─────────┬───────────┘
                │
                ▼
      SQS ▶ NotificationQueueHandler ▶ in-app row / SES email
```

Both gates are logged rather than failing silently, and neither changes how the
request itself is persisted.

## 1. Sentiment analysis

`SentimentAnalysisService` classifies the request subject and description into one of
the four codes seeded in the `sentiment_codes` lookup table:

| Code | Label | Notifiable |
|---|---|---|
| 0 | Good Request | **yes** |
| 1 | Foul Language | no |
| 2 | Depressive or Suicidal | no |
| 3 | Threatening | no |

Keep `SentimentCode` in step with that seed data — the same codes are the target of
`fraud_requests.ref_code`.

The shipped implementation, `LexiconSentimentAnalysisService`, is a deterministic
whole-word lexicon matcher. It is a stand-in for the model-backed classifier tracked
by [ai#7](https://github.com/saayam-for-all/ai/issues/7), and exists so the routing
around it can be built and tested now. The most severe match wins, so a message
containing both an insult and a threat classifies as `THREATENING`.

Each lexicon is overridable from configuration, so the word lists can be tuned or
localised without a code change:

```properties
saayam.sentiment.lexicon.threatening=...
saayam.sentiment.lexicon.depressive=...
saayam.sentiment.lexicon.foul=...
```

Setting a key **replaces** the built-in default for that lexicon rather than adding
to it. When the real classifier lands, it implements `SentimentAnalysisService`,
annotates itself `@Primary`, and nothing else changes.

### What a flagged request does *not* do

A flagged request is not deleted, not rejected, and not moved. It is stored exactly
as before; it simply never reaches volunteer matching. Two deliberate non-goals:

- **No diversion to a `bad_requests` table.** That is
  [database#157](https://github.com/saayam-for-all/database/issues/157); the table
  does not exist in the `virginia_dev_saayam_rdbms` schema yet.
- **No escalation path for codes 2 and 3.** A request flagged depressive/suicidal
  needs a human, not a volunteer broadcast. Suppressing the broadcast is the
  correct half of that; routing it to a trained responder is a separate decision
  that needs Product and safeguarding input.

Only the classification code is logged. The lexicon term that triggered it is carried
on `SentimentAssessment` for tests and moderator tooling, but is request-derived text
and is deliberately kept out of the logs (ba#40 NFR-02).

## 2. Volunteer matching

Routing keys off `request.requestType.requestTypeId`, matching `RequestTypeEnum`:

| Type | Id | Strategy | Why |
|---|---|---|---|
| `IN_PERSON` | 1 | proximity | someone has to physically show up, so distance decides |
| `REMOTE` | 2 | skill | location is irrelevant, so capability decides |
| `UNSPECIFIED` | 0 | skill | fall back to the broader, safer signal |
| absent | — | skill | same |

It keys off the **id**, not the `type` field, because that field is a typed enum on
one lineage and a `String` on the other while the id is an `Integer` on both.

An in-person request with nobody nearby does **not** fall back to skill matching. A
volunteer 200 km away cannot show up, so notifying them would be noise; the no-match
is recorded instead.

### Proximity (in-person)

```sql
FROM user_locations rl                          -- the requester's anchor point
JOIN volunteer_locations vl
       ON ST_DWithin(vl.curr_loc, rl.curr_loc, :radiusMeters)
...
ORDER BY ST_Distance(vl.curr_loc, rl.curr_loc), u.user_id
LIMIT :maxResults
```

The anchor is the **requester's** current location, because `request.req_loc` is free
text with no coordinates. `curr_loc` is a `geography(Point, 4326)` with a GIST index,
so `ST_DWithin` is an index-assisted range scan rather than a full-table distance
computation.

```properties
saayam.matching.in-person.radius-meters=25000   # outer search radius
saayam.matching.in-person.max-volunteers=25     # cap per request, nearest first
```

The cap answers ba#40 open decision **D6** with a default rather than broadcasting to
everyone in radius.

Two situations both produce an empty result and are both ordinary, not errors: nobody
is within the radius, and the requester has no row in `user_locations`.

> **On the spatial microservice.** `saayam-for-all/spatial` owns
> `find_nearest_volunteers`, and this query deliberately mirrors its
> `sql/find_nearest_volunteers.sql`. It is issued in-process because the request
> service already reads `users`, `volunteer_details` and `user_skills` directly and
> has no HTTP client, and because no deployed base URL for that service could be
> found. `VolunteerMatchRepository.findVolunteersNearRequester` is the single seam to
> replace if the spatial service later becomes the sole owner of these tables.

### Skill (remote)

Unchanged from the original notification work: `user_skills.cat_id` is a foreign key
to `help_categories.cat_id`, so a volunteer's "skills" are literally help-category
IDs. Presence of a row in `volunteer_details` is what makes a user a volunteer.

### Known gap

Neither query filters on `users.user_status_id`, because the `user_status` lookup
table has no committed seed data and there is no verifiable "active" value to filter
on. The proximity query also does not read a per-volunteer availability flag —
`volunteer_locations` has no such column in this schema, unlike the `volunteer_details.availability`
the spatial service reads. Add both filters once the DB team seeds them; each is a
one-line `WHERE` clause.

## 3. Dispatch

Each matched volunteer becomes one `IN_APP` recipient, plus one `EMAIL` recipient when
an address is on file. A volunteer with no email is still reachable in-app rather than
being dropped. From there the existing pipeline takes over: SQS →
`NotificationQueueHandler` → in-app row and/or SES email.

## Related work

| Item | Where |
|---|---|
| Matching + notification BRD | [ba#40](https://github.com/saayam-for-all/ba/issues/40) |
| Remote & in-person matching | [ba#88](https://github.com/saayam-for-all/ba/issues/88) |
| Sentiment analysis | [ai#7](https://github.com/saayam-for-all/ai/issues/7) |
| `bad_requests` table | [database#157](https://github.com/saayam-for-all/database/issues/157) |
| ML matcher (separate, currently broken) | [ai#157](https://github.com/saayam-for-all/ai/issues/157) |
| Geospatial volunteer lookup | [spatial#13](https://github.com/saayam-for-all/spatial/issues/13) |
| Skill-based matching data model | [volunteer#93](https://github.com/saayam-for-all/volunteer/issues/93) |

### A discrepancy worth resolving

`RequestTypeEnum` (both lineages) says `UNSPECIFIED(0), IN_PERSON(1), REMOTE(2)`, but
the comment in `database/ddl/Tables/ddl_request_type.sql` says `0 INPERSON, 1 HYBRID`.
The code is the authority here because it is what the running service uses and both
lineages agree, but the DDL seed should be reconciled — related to
[request#78](https://github.com/saayam-for-all/request/issues/78).
