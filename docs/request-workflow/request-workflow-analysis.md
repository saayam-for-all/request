# Saayam Request Workflow — Evidence and Architecture Reconstruction

## Scope

This document reconstructs Issue #1, “Come up with request workflow and state diagram,” from repository evidence. It deliberately separates:

- **Implemented on `dev`** — executable behavior at the investigated commit.
- **Partially implemented** — concrete code exists on an unmerged branch, or scaffolding exists without an end-to-end path.
- **Historical/intended** — explicit design behavior in the original Draw.io, issue comments, requirements branch, or older code, but no working end-to-end implementation on `dev`.
- **Unknown** — the repository does not define the policy; no transition is invented.

The Draw.io files are the primary artifacts. The diagrams present a complete evidence-backed lifecycle while making implementation maturity impossible to confuse.

## Repository, branch, and commit investigated

- Repository: `saayam-for-all/request`
- Issue: [#1 — Come up with request workflow and state diagram](https://github.com/saayam-for-all/request/issues/1)
- Authoritative implementation branch: `dev`
- Exact investigated SHA: `3b1f6f341a7e7bce38d676e0041189816227c22f`
- Working branch: `docs/issue-1-request-workflow`
- `origin/main`: `9f463f5` — contains the historical Draw.io but not the current request implementation.
- `origin/test`: `1c9b2d5` — diverged from `dev`; neither tip contains the other.
- `dev` merge base with `test`: `29ab7b168120d7ccb41f0f4467388370ff22ae33`.

The repository was fetched and `dev` was fast-forward checked before analysis. No application code was modified.

## Original Issue and Draw.io evidence

Issue #1 asks for both a request workflow and a state-transition diagram. Its two comments identify the pages “User Request Handler Service” and “Use Case - New User Request.” The assignment/status timeline was also inspected.

The inaccessible Google Drive artifact is not needed: PR #32 / commit `313cf5d` checked the original file into `main`. The user-supplied copy at `/Users/klsterfx/Downloads/Saayam-Request-Service-Flow.drawio` was also parsed. Both contain six pages:

| Page | Material evidence |
| --- | --- |
| `User Request Handler Service` | New/existing request decision, DB registration/update, pending status, requester notifications, volunteer matching, batches/timeouts, AI fallback, OTP handshake, assignment, in-progress work, completion, additional volunteers and volunteer organizations. |
| `Use Case - New User Request` | Five sequence-style cases: first-batch volunteer accepts; no match and AI fallback; later-batch acceptance; lead plus helpers; lead plus external organization. |
| `Notes` | Explicit meeting notes for S3 storage, assignment owner, OTP handshake, lead working alone/helpers/org, org selection, and special calamity matching. |
| `Request-APIs` | API Gateway, create/get/list/update/delete Lambda functions, and a request datastore. |
| `Page-1` | Step Functions-style new/existing request flow, SNS success/failure/completion notifications, and new-request notification to Volunteer Service. |
| `Page-6` | Thirteen connector-only cells and no process vertices; it contains no recoverable workflow semantics. |

The new diagrams preserve that architectural depth but do not present the 2024 model as current production behavior.

## Current implementation on `dev`

### API entry points

`RequestController` exposes:

| Operation | Route | Behavior |
| --- | --- | --- |
| Create | `POST /dev/requests/v1.0.0/requests` | Validates DTO, resolves reference rows, creates request with `CREATED`, persists it, optionally persists guest details, and returns 201. |
| Read one | `GET /{requestId}?requesterId=...` | Returns a non-deleted request matching request ID and requester ID. |
| List | `GET ?requesterId=...` | Returns requester-owned requests excluding `DELETED`. |
| Update | `PUT /{requestId}` | Updates mutable request fields; rejects `CANCELLED`; does not update request status. |
| Cancel | `POST /{requestId}/cancel` | Changes any non-deleted, non-cancelled request to `CANCELLED`. |
| Resume | `POST /{requestId}/resume` | Only accepts `CANCELLED`; always changes it to `CREATED`. |
| Delete | `DELETE /{requestId}` | Soft-deletes any non-deleted request by assigning `DELETED`. |

Evidence: `RequestController.java:43,137-220`; `RequestServiceImpl.java:78-227`; `RequestRepository.java:23-50`.

The dispatcher Lambda is incomplete relative to the Spring controller. It normalizes `/requests/v0.0.1` to `/api` and dispatches only create plus help-category/metadata operations; the standalone get/update/cancel/resume/delete handlers exist but are not routed by this dispatcher. Evidence: `RequestDispatcherHandler.java:37-132`; the individual classes under `requesthandler/`.

### Create flow

1. Bean validation runs because the controller uses `@Valid` (`RequestController.java:137-148`, `RequestDTO.java:14-63`).
2. The service checks required enum/reference IDs are non-null (`RequestServiceImpl.java:229-250`).
3. Priority, type, help category, request-for, status, and lead indicator are resolved from repositories (`:83-88`, `:236-285`).
4. The service ignores the incoming status value and assigns status ID 1, `CREATED` (`:87`).
5. The request is built with submitted/last-updated timestamps and persisted (`:90-100`, `:301-330`).
6. Guest details are conditionally persisted (`:102-117`).
7. A success response is returned (`:119-121`).

There is no current event publication, requester notification, volunteer discovery, status advancement, Step Functions execution, or retry after save on `dev`.

### Current status mutations

All current status writes are in `RequestServiceImpl`:

- New request → `CREATED`: line 87.
- Any active state except already-cancelled → `CANCELLED`: lines 189-205.
- `CANCELLED` → `CREATED`: lines 210-226.
- Any non-deleted state → `DELETED`: lines 169-184.

`updateRequestFields` does not copy `requestStatus` from the DTO. No admin, volunteer, scheduler, queue consumer, or Step Functions handler mutates request status on `dev`.

### Data and side effects

- Request persistence: `requestRepository.save`.
- Optional guest-details persistence: `requestGuestDetailsRepository.save`.
- State-change timestamp: `lastUpdatedAt` for cancel, resume, and delete.
- `servicedAt` can be supplied during create/update, but no completion action sets it automatically.
- No status-change audit table or actor record exists.
- `DELETED` is a soft-delete marker filtered from normal reads.

## Historical design intent

The original Draw.io provides explicit architecture evidence for this intended flow:

1. Receive and validate a new request.
2. Register it and persist a pending status.
3. Store request detail in S3 and notify requester.
4. Ask Volunteer Module to profile-match volunteers.
5. Notify a batch of `X` volunteers and wait `Y` minutes.
6. If no acceptance, notify a later batch; when exhausted, call AI.
7. If AI responds, send the response and complete the request; otherwise notify processing failure.
8. If a volunteer accepts, generate/send OTP and complete requester/volunteer handshake.
9. Finalize lead assignment and transition to in-progress.
10. Lead works alone, adds Saayam helpers, or selects an accepting volunteer organization.
11. Persist completion and notify participants.

This is supported by the first, second, third, and fifth Draw.io pages. It is also restated as requirements on `origin/beelapranay/request-requirements-doc` at commit `9695762`; that document explicitly says it derives from the historical Draw.io, so it is corroborating design evidence rather than evidence of implementation.

## Relevant commit, PR, and branch findings

| Evidence | Finding | Classification |
| --- | --- | --- |
| PR #56, commits `47a7eec`, `1b20195`, merged into `dev` as `3b1f6f3` | Current create/read/update/cancel/resume/delete model and new relational schema mappings. | Implemented |
| Commit `d3e10c8` | SQS configuration, producer, listener, and controller publishing were commented out before the current `dev` tip. | Historical scaffold disabled on `dev` |
| `origin/feature/StepfunctionCreation_Rishab`, commit `9ec8635` | Starts a Step Functions execution after save, but catches failure and still returns success. State machine contains only `ProcessRequest` and `LogRequest` Pass states—no lifecycle orchestration. | Partial |
| PR #66 / commit `3615547` | Adds request event publication after commit to SQS; Lambda consumes and dispatches through SES/SNS with partial-batch retry/DLQ guidance. Events include create/update/cancel/resume/delete. Not on `dev`. | Partial |
| PR #70 / branch `feature/issue-14-lead-helping-volunteer-test`, commit `6fb4f31` | Adds persisted `LEAD`/`HELPING` assignments, lead reassignment, removal, and tests. Assignment does not mutate status. | Partial |
| Branch `feature/issue-14-lead-helping-volunteer`, commit `14da41e` | Combines assignment with notification branch; publishes `VOLUNTEER_CHOSEN` for new/reassigned lead. Recipient resolution still targets requester/guest, not a volunteer contact service. | Partial |
| PR #61 / commit `dba0377` | Adds notes and helper volunteer CRUD with an `X-Actor-UserId` lead check. It is a collaboration UI backend, not discovery/acceptance/handshake. | Partial |
| Commit `db30138` | Earlier model had `VolunteersAssigned` and `HELPER`/`LEAD` enum, confirming assignment has long-standing design intent. | Historical/partial lineage |
| PR #68 / commit `3c72455` | Packages/deploys request service for Lambda and adds supporting AWS configuration; does not add lifecycle transitions. | Partial infrastructure |
| PR #65 / commit `7306d7e` | Adds fallback help category for create; no lifecycle change. | Partial enhancement |
| PR #69 / commit `61ab990` | Persists structured healthcare request information transactionally; no lifecycle change. | Partial enhancement |
| `origin/feature/other-user-flow`, commit `fc61074` | Calls a volunteer-service client to create/resolve a user for “request for other”; not volunteer matching. | Partial integration |
| File-attachment commits `55d460c`, `b76f45b`, `8b44cc1` | S3 work is for attachments; it does not implement the historical “store entire request after registration” operation. | Partial, different scope |
| PR #32 / commit `313cf5d` | Adds the original six-page Draw.io to `main`. | Historical design |

No branch or commit implements AI calls, OTP generation/verification, volunteer batch timeout scheduling, volunteer acceptance, status advancement to pending/in-progress/completed, rating submission, or a transition validator.

## Lifecycle reconstruction

### Implemented path

`Requester → Spring API → validation/reference lookup → relational persistence → CREATED → synchronous response`

From `CREATED`, requester-addressed APIs currently support update-in-place, cancel, resume, and soft delete. Because guards only test cancellation/deletion, manually populated future states would also be cancellable/deletable even though no API can reach them.

### Partially implemented continuation

The unmerged work provides three useful building blocks, but they are not connected into one lifecycle:

- An after-save Step Functions trigger whose state machine only logs.
- An after-commit request-event notification pipeline through SQS/Lambda/SES/SNS.
- Manual lead/helper assignment and reassignment persistence, including a chosen-volunteer event.

These support the historical direction but do not implement discovery, acceptance, timeout, handshake, or state advancement.

### Historical/intended fulfillment path

The evidence-backed target path is:

`CREATED → PENDING_VOLUNTEER_ASSIGNMENT → volunteer search/batched notification → acceptance → OTP handshake/assignment → IN_PROGRESS → work alone/helpers/org → COMPLETED → ratings`

Failure to match or accept can lead to AI fallback. The original diagram treats a successful AI response as a completion path and a missing AI response as processing failure. No declared `FAILED` status exists, so failure remains an operational outcome rather than a diagrammed request state.

Cancellation, resume, and delete are current side flows. Cancellation is reversible today only by resetting to `CREATED`; the repository has no previous-status field, so restoring the pre-cancel state is impossible without a schema/policy change.

## State inventory

All nine values were introduced together in `e14336c`/`e01d05e` and have not changed. Evidence: `RequestStatusEnum.java:6-22`, `data.sql:1-13`, and `git blame`.

| ID | State | Meaning supported by evidence | Entry/exit ownership | Dev reachability |
| ---: | --- | --- | --- | --- |
| 0 | `UNSPECIFIED` | Seed/default sentinel. No lifecycle semantics are defined. | No actor enters or leaves it in code. | Unreachable through service code. |
| 1 | `CREATED` | Persisted initial request; also the hard-coded resume target. | Entered by backend on create and resume. Leaves via requester cancel/delete; intended to advance to pending. | Reachable and written. |
| 2 | `PENDING_VOLUNTEER_ASSIGNMENT` | Request awaits matching/acceptance. Name and old “Pending” diagrams agree. | Intended backend/orchestrator entry after registration; Volunteer/Request workflow leaves after assignment. | Declared/seeded only. |
| 3 | `IN_PROGRESS` | Fulfillment started with assigned lead. | Intended handshake/assignment entry; volunteer/requester workflow completes or requester cancels. | Declared/seeded only. |
| 4 | `COMPLETED` | Help fulfilled. | Intended approved completion entry; ratings occur afterward. | Declared/seeded only. |
| 5 | `CANCELLED` | Request intentionally stopped. | Entered via requester-addressed cancel endpoint; leaves only via resume or delete. | Reachable and written. |
| 6 | `DELETED` | Soft-deleted record hidden from active queries. | Entered via requester-addressed delete endpoint; no exit. | Reachable, written, terminal in code. |
| 7 | `RATED_BY_REQUESTER` | Requester rating marker. | No API, model, rating value, or transition exists. | Declared/seeded only. |
| 8 | `RATED_BY_VOLUNTEER` | Volunteer rating marker. | No API, model, rating value, or transition exists. | Declared/seeded only. |

## State transition matrix

Legend: **I** implemented on `dev`; **P** concrete partial branch support; **H** historical/intended evidence only.

| From | To | Trigger / actor | Guard | Side effects | Maturity and evidence |
| --- | --- | --- | --- | --- | --- |
| Start | `CREATED` | Requester creates; backend assigns status | DTO/reference validation succeeds | Request + optional guest details; timestamps | **I** — `RequestServiceImpl:80-121` |
| `CREATED` | `CREATED` | Requester updates | Active and not cancelled | Mutable fields + `lastUpdatedAt` | **I** — `:149-165` |
| `CREATED` | `PENDING_VOLUNTEER_ASSIGNMENT` | Backend/orchestrator starts matching | Persisted request available | Intended notification/matching kickoff | **H**, with Step Functions/notification scaffolds **P** — original Draw.io; `9ec8635`; `3615547` |
| `PENDING_VOLUNTEER_ASSIGNMENT` | `PENDING_VOLUNTEER_ASSIGNMENT` | Timeout; notify next volunteer batch | More candidates remain | Matching/notification attempt | **H** — original Draw.io use case 3 |
| `PENDING_VOLUNTEER_ASSIGNMENT` | `IN_PROGRESS` | Volunteer accepts; requester/volunteer handshake succeeds | Matched acceptance + OTP/confirmation | Lead assignment; status/timestamp; notifications | Assignment persistence **P** (`6fb4f31`); acceptance/OTP/status transition **H** |
| `PENDING_VOLUNTEER_ASSIGNMENT` | `COMPLETED` | AI fallback returns usable answer | Matching exhausted/no acceptance | AI response to requester; completion update | **H** — original Draw.io cases 1/2; no AI code |
| `IN_PROGRESS` | `IN_PROGRESS` | Lead adds/removes/reassigns helpers or org collaboration continues | Authorized lead/workflow | Assignment changes/notifications | Lead/helper assignment **P** (`6fb4f31`, `14da41e`); org path **H** |
| `IN_PROGRESS` | `COMPLETED` | Volunteer/requester-approved completion | Completion policy unknown | Intended `servicedAt`, completion notification | **H** — original Draw.io; no completion endpoint |
| `COMPLETED` | `RATED_BY_REQUESTER` | Requester submits rating | Rating policy/data model missing | Unknown | Enum intent only; no executable evidence |
| `COMPLETED` | `RATED_BY_VOLUNTEER` | Volunteer submits rating | Rating policy/data model missing | Unknown | Enum intent only; no executable evidence |
| Active non-deleted, non-cancelled | `CANCELLED` | Requester-addressed cancel API | Not already cancelled | `lastUpdatedAt`; save | **I** — `:189-205` |
| `CANCELLED` | `CREATED` | Requester-addressed resume API | Must be cancelled | `lastUpdatedAt`; save | **I** — `:210-226` |
| Any non-deleted state | `DELETED` | Requester-addressed delete API | Not already deleted | `lastUpdatedAt`; soft delete | **I** — `:169-184` |

The state diagram shows rating states as **questionable terminal markers**, not as a credible linear pair. Two independent actors can rate in either order, but one status column cannot represent “both rated” or preserve `COMPLETED`. Ratings should likely be events/records orthogonal to lifecycle state; that is an architectural conclusion from the current schema, not a claim that a replacement model is already approved.

## Implemented versus intended gaps

| Lifecycle capability | Dev | Evidence beyond dev | Gap |
| --- | --- | --- | --- |
| Validation and persistence | Implemented | Additional-info/fallback-category branches | Current `schema.sql` does not match entity mappings. |
| Requester success response | Implemented synchronously | Notification pipeline branch | No requester delivery on `dev`. |
| Pending assignment status | Enum/seed only | Historical Draw.io | No trigger or write. |
| Volunteer discovery/profile match | Missing | Historical Draw.io only | No client/API contract. |
| Volunteer batches/timeouts/retry | Missing | Historical Draw.io only | No scheduler, queue workflow, candidate cursor, or attempt records. |
| Volunteer assignment | Missing | PR #70/manual assignment branch | No acceptance flow and no status update. |
| Notifications | Commented-out SQS on dev | PR #66 and `3615547` | Unmerged; AWS wiring still environment-dependent. |
| Step Functions | Missing | `9ec8635` Pass/Pass state machine | No orchestration tasks/choices/retries. |
| AI fallback | Missing | Historical Draw.io/requirements branch | No module client or response model. |
| OTP handshake | Missing | Historical Draw.io/notes | No OTP owner, store, expiry, or verification API. |
| Lead/helpers | Missing | PR #70 and PR #61 | Parallel models (`VolunteerAssignment` vs helper CRUD) need consolidation. |
| Volunteer organization | Missing | Historical Draw.io | No model/API/integration. |
| Completion | Enum/seed only | Historical Draw.io | No completion API, actor policy, timestamp behavior, or notification. |
| Ratings | Enum/seed only | No implementation history | Status model is insufficient for two independent ratings. |
| Failure/retry | HTTP exceptions only | Notification DLQ guidance on branch | No request failure state or workflow retry record. |

## Authentication and authorization observations

There is no Spring Security/JWT/authentication implementation or dependency on `dev`. Ownership is enforced only by querying with both caller-supplied `requesterId` and `requestId` (`RequestRepository.java:34-50`). Create and update obtain requester ID from the request DTO, while other endpoints take it as a request parameter. The Lambda handlers likewise trust path/body values.

Therefore the diagram labels the current actor as a **requester-addressed API call**, not a verified authenticated requester. PR #61 introduces a lead-only `X-Actor-UserId` comparison for helper operations, but it is unmerged and header identity is still not cryptographically established in this repository.

## Schema inconsistencies

`schema.sql` is stale relative to the current entities:

- SQL uses `request_id`, `request_user_id`, `request_status_id`, `request_category_id`, `city_name`, `zip_code`, and `lead_volunteer_user_id` (`schema.sql:54-76`).
- Entity mappings use `req_id`, `req_user_id`, `req_status_id`, `req_cat_id`, `req_loc`, `req_subj`, `req_doc_link`, and `req_islead_id` (`Request.java:41-128`).
- `RequestStatus` maps `req_status_id`/`req_status`, while SQL creates `request_status_id`/`request_status`.
- No Flyway/Liquibase migration directory exists; only `schema.sql` and `data.sql` are present.

These contradictions mean repository-local schema bootstrap is not reliable evidence that the current JPA model runs against a clean database.

## Test coverage findings

- Main-source compilation succeeds at the investigated `dev` SHA.
- Test compilation fails before tests run because inherited tests reference removed fields/classes and pass enum values where entities now store strings. These failures predate and are independent of documentation changes.
- Existing controller tests mock service calls. They do not execute `RequestServiceImpl` transition guards or assert persisted status mutations.
- Entity tests mention `IN_PROGRESS`/`COMPLETED`, but only as object getter/setter fixtures; they do not prove lifecycle reachability.
- Feature assignment and notification branches add focused tests for their own components, but those branches remain unmerged and still do not implement the full lifecycle.

## Unresolved architecture questions

1. What component owns the authoritative lifecycle—Request Service, Volunteer Service, or Step Functions?
2. What exact event moves `CREATED` to `PENDING_VOLUNTEER_ASSIGNMENT`?
3. Who owns candidate selection, batch size, timeout, retry count, acceptance concurrency, and idempotency?
4. Is OTP still required, and which service generates, stores, expires, and verifies it?
5. What completion actor/policy is authoritative, and should completion set `servicedAt` automatically?
6. Should cancellation be allowed from every active state, including `COMPLETED` and rating markers?
7. Should resume restore the pre-cancel state? Current schema cannot; code always returns to `CREATED`.
8. Is `DELETED` the final retention state, or should completed/cancelled records be archived under a separate policy?
9. Are requester/volunteer ratings lifecycle states at all? The current single status field cannot represent both ratings.
10. What should happen when matching and AI fallback both fail? No `FAILED` state is declared.
11. How does calamity-mode matching differ, and who activates it? Only `isCalamity` storage and historical notes exist.
12. Which volunteer-assignment model should survive: PR #61 helper CRUD, PR #70 `LEAD`/`HELPING` assignments, or a Volunteer Service-owned model?

## Evidence references

### Current `dev`

- `src/main/java/org/sfa/request/controller/RequestController.java`
- `src/main/java/org/sfa/request/service/impl/RequestServiceImpl.java`
- `src/main/java/org/sfa/request/model/enums/RequestStatusEnum.java`
- `src/main/java/org/sfa/request/model/entity/Request.java`
- `src/main/java/org/sfa/request/model/entity/RequestStatus.java`
- `src/main/java/org/sfa/request/repository/RequestRepository.java`
- `src/main/java/org/sfa/request/requesthandler/*.java`
- `src/main/java/org/sfa/request/config/SQSConfig.java`
- `src/main/java/org/sfa/request/listener/SQSListener.java`
- `src/main/java/org/sfa/request/service/impl/SQSServiceImpl.java`
- `src/main/resources/schema.sql`
- `src/main/resources/data.sql`
- `src/test/java/org/sfa/request/**`

### History, branches, and PRs

- PR #32 / `313cf5d` — historical Draw.io.
- PR #56 / `47a7eec`, `1b20195`, merge `3b1f6f3` — current `dev` implementation.
- PR #61 / `dba0377` — notes/helper volunteer backend.
- PR #65 / `7306d7e` — fallback category.
- PR #66 / `3615547` — async notification pipeline.
- PR #68 / `3c72455` — Lambda deployment.
- PR #69 / `61ab990` — healthcare additional information.
- PR #70 / `6fb4f31` — lead/helping volunteer assignments.
- `origin/feature/StepfunctionCreation_Rishab` / `9ec8635` — Step Functions trigger scaffold.
- `origin/feature/issue-14-lead-helping-volunteer` / `14da41e` — assignments plus notifications.
- `origin/feature/other-user-flow` / `fc61074` — requester-for-other Volunteer Service client.
- `origin/beelapranay/request-requirements-doc` / `9695762` — historical-design-derived requirements.
- `db30138` — earlier user/volunteer assignment schema.
- `d3e10c8` — disabled SQS implementation.

## Diagram interpretation rules

- Green solid shapes/connectors: implemented on `dev`.
- Amber shapes/connectors: concrete partial implementation outside `dev` or incomplete scaffolding.
- Blue dashed shapes/connectors: historical/intended behavior with repository evidence but no implementation.
- Purple shapes: external actor/service/module.
- Red shapes: error/failure outcome.
- Dark double-bordered nodes: terminal or code-terminal states.

These rules are embedded as legends in both Draw.io files.
