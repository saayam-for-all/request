# Request status reconciliation (Issue #78)

Status: **blocked on deployed dev RDS evidence and Request/Database-team sign-off**.

This investigation intentionally makes no status-ID changes. The application uses numeric foreign keys, so changing the Java enum or a seed file before checking deployed lookup rows and existing requests could silently reinterpret data.

## Evidence inspected

The investigation used these repository revisions on 2026-09-16:

- `saayam-for-all/request` `origin/dev` at `3b1f6f341a7e7bce38d676e0041189816227c22f`.
- `saayam-for-all/database` `main` at `c41785842162cdedd23f009feacdca311c657d1d`.
- `saayam-for-all/request.wiki` `master` at `a31991d`.
- Request issues #1, #38, #47, #77, #78, and #80; request PR #74; database issue #226; and database PR #86.
- All request-repository references to `RequestStatusEnum`, `RequestStatus`, `request_status`, status names, persisted status IDs, and status comparisons.

No comment or review on those issues/PRs records approval of a canonical ID table by both teams. Issue #47 still owns the enum lookup/API completeness work. Issue #78 owns the cross-repository name/ID reconciliation; it should not rewrite unrelated enum APIs.

## Deployed RDS verification gate

No usable dev-RDS connection is available in the investigation environment. `DB_DEV_HOST_URL`, `DB_DEV_USERNAME`, and `DB_DEV_PASSWORD` are unset. The configured AWS account has no RDS instances, Secrets Manager secrets, or SSM parameters in `us-east-1`. Consequently, the acceptance criterion requiring the complete deployed result remains open and no result has been fabricated.

Run [`rds-verification.sql`](rds-verification.sql) against dev RDS and attach its complete output to issue #78. The queries are wrapped in a read-only transaction and use `req_status_id`, the column declared by the current database DDL and JPA entities.

## Conflicting definitions confirmed

| Source | Definition |
| --- | --- |
| Request Java enum and local `data.sql` | `0 UNSPECIFIED`, `1 CREATED`, `2 PENDING_VOLUNTEER_ASSIGNMENT`, `3 IN_PROGRESS`, `4 COMPLETED`, `5 CANCELLED`, `6 DELETED`, `7 RATED_BY_REQUESTER`, `8 RATED_BY_VOLUNTEER` |
| Database `request_status.csv` and wiki enum table | `0 CREATED`, `1 MATCHING_VOLUNTEER`, `2 IN_PROGRESS`, `3 RESOLVED`, `4 CANCELLED`, `5 DELETED`, `6 RATED_BY_REQUESTER`, `7 RATED_BY_VOLUNTEER` |
| Database DDL comments and wiki API page | `0 CREATED`, `1 MATCHING_VOLUNTEER`, `2 MANAGED`, `3 CLOSED`/`RESOLVED`, `4 CANCELLED`, `5 DELETED` |
| Wiki state diagram text | Adds `REASSIGNMENT_REQUESTED`; its July 2026 image update adds `VolunteerNotFound`; it also describes delegation without a consistent enum row |
| Database issue #226 | Requires a `VolunteerNotFound` outcome after three matching batches separated by approximately 24 hours; no linked implementation or assigned ID exists |
| Request PR #74 | Documents the request-code mapping and concludes ratings are independent events/records, not mutually exclusive lifecycle states |

The request service currently writes or compares only these IDs through `RequestStatusEnum`: create/resume use `CREATED`, cancel uses `CANCELLED`, and delete/list/read use `DELETED`. If the deployed table follows the database CSV, current code resolves those operations to `MATCHING_VOLUNTEER`, `DELETED`, and `RATED_BY_REQUESTER`, respectively. If deployed RDS instead follows the request-local seed, changing the enum to the CSV mapping would create the inverse corruption. This is why the RDS gate cannot be bypassed.

## Proposed lifecycle names (awaiting sign-off)

The following semantic model reconciles the workflow names. The IDs shown are a **conditional proposal only** for the case where RDS confirms the database CSV mapping. They are not approved for deployment.

| ID | Canonical name | Meaning | Classification | Evidence | Migration impact |
| ---: | --- | --- | --- | --- | --- |
| 0 | `CREATED` | Persisted initial request | Active | Database CSV, wiki, initial workflow state | Java/local seed change required if RDS confirms ID 0 |
| 1 | `MATCHING_VOLUNTEER` | Matching and batched volunteer notification are active | Active | Database CSV, wiki, workflow issues | Replaces the synonymous `PENDING_VOLUNTEER_ASSIGNMENT` name |
| 2 | `IN_PROGRESS` | Accepted/verified volunteer is fulfilling the request | Active | Database CSV, request enum, wiki | Java ID change required if RDS confirms ID 2 |
| 3 | `RESOLVED` | Help has been completed successfully | Terminal lifecycle outcome | Database CSV, wiki, database PR #86 | Replaces the synonymous `COMPLETED` name |
| 4 | `CANCELLED` | Request was cancelled | Terminal unless an explicit resume policy applies | All current sources | Java ID change required if RDS confirms ID 4 |
| 5 | `DELETED` | Soft-deleted and excluded from active reads | Terminal | All current sources and service behavior | Java ID change required if RDS confirms ID 5 |
| 6 | `RATED_BY_REQUESTER` | Historical lookup value; rating itself belongs in an independent record/event | Deprecated, not lifecycle | Existing database CSV/wiki and PR #74 analysis | Reserve; do not reuse until RDS counts and a migration prove safe |
| 7 | `RATED_BY_VOLUNTEER` | Historical lookup value; rating itself belongs in an independent record/event | Deprecated, not lifecycle | Existing database CSV/wiki and PR #74 analysis | Reserve; do not reuse until RDS counts and a migration prove safe |
| 8 | `VOLUNTEER_NOT_FOUND` | Three matching batches completed without a volunteer | Exception/manual-resolution state | Database issue #226 | New row; appended to avoid reusing historical IDs |
| 9 | `REASSIGNMENT_REQUESTED` | Current assignment must be replaced | Active | Issue #78 and wiki workflow | New row |
| 10 | `DELEGATED` | Fulfillment has been delegated to an organization/party | Active | Issue #78 and workflow documentation | New row |

Names use uppercase snake case to match Java and existing database conventions. `MATCHING_VOLUNTEER` is preferred over `PENDING_VOLUNTEER_ASSIGNMENT`, and `RESOLVED` over `COMPLETED`, because they are already used by the database CSV and wiki. `MANAGED` and `CLOSED` are stale DDL-comment terms, not distinct states.

`UNSPECIFIED` has no request-service entry path and no lifecycle meaning. If RDS confirms `CREATED=0`, remove `UNSPECIFIED` completely rather than persist it or retain a colliding sentinel. If RDS instead contains `UNSPECIFIED=0` and `CREATED=1`, preserve deployed lifecycle IDs and decide separately whether the unused sentinel row can be removed; do not shift all IDs.

Ratings should be modeled as independent records/events so both participants can rate in either order while the request remains resolved. IDs 6 and 7 must remain reserved until the deployed lookup and request counts prove whether historical rows use them. They must never be reassigned directly to new lifecycle meanings.

## Decision after RDS output

1. If deployed rows match the database CSV, update Java and request-local seed names/IDs to the conditional table, append IDs 8-10, and keep 6-7 as explicit deprecated compatibility values until their request counts are zero or migrated.
2. If deployed rows match the request-local seed, preserve deployed lifecycle IDs. Rename only semantically equivalent states after checking every consumer, append new IDs above the highest deployed ID, and coordinate a database-repository seed correction.
3. If deployed rows contain a third mapping, use the grouped request counts to prepare an explicit, idempotent migration. The migration must validate old `(id, name)` pairs, update lookup names and referencing rows in one transaction, reject a partially migrated state, and include before/after validation plus rollback guidance.
4. Obtain explicit Request-team and Database-team approval of the resulting table before changing application IDs, canonical database seed data, or wiki mappings.

Only after those steps should tests be added that assert required IDs, unique IDs/names, and exact Java/local-seed parity. Locking the current unverified mapping into tests would perpetuate the mismatch rather than prevent it.

## Documentation follow-up

After sign-off, update the request repository workflow documentation (preferably by rebasing/adjusting PR #74 rather than duplicating it) and the wiki pages `Request Enum Tables – Database Documentation` and `Help-Request State Diagram and Workflow`. The wiki's `Request APIs Documentation` also embeds the stale `MANAGED` mapping and should be corrected in the same wiki commit.
