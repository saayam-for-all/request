# Issue #88 implementation plan and gap analysis

Base: `origin/dev` at `15abc1035f4f5d84b5b245b4052a8494a2f051a3` (fetched 2026-10-28). This plan was written before application edits.

## 2026-10-28 continuation

The existing Issue #88 branch remains based on that `dev` commit. Draft PR #92
contains the Issue #78 name-based status fix but has not merged into `dev`; no
numeric status mapping or new status write is copied here. Issues #79 and #80
remain open with no callback implementation or approved persistence boundary.
The pure first/next batch Lambda is now implemented and tested against the
actual ASL payload/output shape. Its batch-size setting and the three-window
policy still need product/Database approval. The development CloudFormation
template validates through the AWS API but has not been deployed, because the
remaining Lambda ARNs and dev infrastructure are absent.
The matching Lambda now rejects a stale requester/category/type/calamity value,
non-matchable request status, and calamity requests without an approved routing
policy before calling the existing volunteer query.
Database issue #157's latest comment says the bad-request table was built but
still needs a PR; no merged database PR or deployed schema was verified. Issues
#79 and #80 still have no implementation PR or approved new API contract, and
PR #91 has no review comments yet. The available AWS account exposes no Step
Functions machines or Lambda functions in `us-east-1`; an end-to-end dev run is
therefore unavailable here.
The continuation built a shaded jar containing the batch handler, passed the
21 Python ASL path simulations, and passed 130 runnable Java tests (four
skipped). The full Java suite retains only the two pre-existing Spring context
errors caused by the absent JDBC URL. AWS returned `result: OK` and zero
diagnostics for the Standard ASL rendered with synthetic Lambda ARNs.

## Evidence and current architecture

The REST `RequestController` and Lambda `CreateRequestHandler` call `RequestServiceImpl.createRequest`. It validates lookup IDs, writes `request` (and optional guest data), populates lead/helper assignments from `volunteers_assigned`, then calls `NotificationEventService`. That service runs the local lexicon `SentimentAnalysisService`, resolves category or proximity matches, and after transaction commit sends an SQS event. The SQS listener/handler dispatches in-app and SES notifications. No Step Functions execution is started on `dev`. Cancel/delete/resume only update request status; they do not stop an execution. `ddl-auto=none`, so JPA additions require a Database team migration.

PRs [#82](https://github.com/saayam-for-all/request/pull/82), [#84](https://github.com/saayam-for-all/request/pull/84), and [#86](https://github.com/saayam-for-all/request/pull/86) are merged on `dev` and supply notification, assignment, classification gate, and matching seams. [#74](https://github.com/saayam-for-all/request/pull/74) remains draft. The historical `feature/StepfunctionCreation_Rishab` has a two-Pass-state ASL and a `StartExecution` adapter based on a timestamp; it does not implement the diagram and is not merged.

The [wiki state and four flow diagrams](https://github.com/saayam-for-all/request/wiki/Help-Request-State-Diagram-and-Workflow) were visually inspected. Part 1 includes optional transcription/category suggestions, sentiment, and good/bad persistence. Part 2 branches lead volunteer, calamity, in-person/remote match, notification, acceptance, and manual review. Part 3 includes two-party OTP, bounded retry, admin reassignment/cancellation. Part 4 includes helpers/delegation, resolution, and two ratings. The wiki places `IN_PROGRESS` before OTP; Issue #88 requires it only after verified OTP, which is the safer gate proposed here.

The `Unified Sheet for Step Functions.xlsx` from [#77](https://github.com/saayam-for-all/request/issues/77) was not found in accessible repository files, history, GitHub search, or wiki. Its 36 exact row contracts and conditions remain **unverified**. The ASL therefore models verified diagram decisions and explicit interfaces, not a claim that all 36 rows are implemented.

## Required lifecycle and ownership

Target: submission → approved ML classification → accepted persistence → matching → volunteer acceptance → both-party verification → work → resolution → independent ratings. Rejection storage and safeguarding need approved policy/schema. Existing Create Request must remain the active path until [#80](https://github.com/saayam-for-all/request/issues/80) agrees the atomic submission/StartExecution/persistence boundary. [#79](https://github.com/saayam-for-all/request/issues/79) owns callback token persistence and authenticated resolution. [#78](https://github.com/saayam-for-all/request/issues/78) owns canonical status IDs and migration. [#77](https://github.com/saayam-for-all/request/issues/77) owns the full 36-block skeleton and deployment from CI. [#38](https://github.com/saayam-for-all/request/issues/38) is the parent workflow issue.

The latest #80 product comment explicitly calls for classification before good/bad persistence. The current `dev` path persists before its lexicon gate and leaves flagged requests in `request`; these are incompatible. The `bad_requests` table is tracked by `database#157`, not present here. The Java `RequestStatusEnum` begins `UNSPECIFIED(0), CREATED(1)` while the database CSV begins `CREATED(0), MATCHING_VOLUNTEER(1)` according to #78; live RDS was not verified. Issue #88's type IDs `IN_PERSON(0), REMOTE(1)` conflict with running `RequestTypeEnum` (`IN_PERSON(1), REMOTE(2)`). The running type mapping is used for routing; status writes beyond the current path are blocked until #78 is approved. Ratings should be independent records/events as argued in #74, because both parties can rate in either order.

## Requirement-to-implementation matrix

`T` means transient retry with bounded exponential backoff, `C` means catch to a controlled failure. All Lambda outputs must be validated before a choice or status write. The source column names the verified evidence for the requirement.

| Section / task | Input → output | Existing implementation / required integration | Retry, timeout; success / failure | Status | Source |
|---|---|---|---|---|---|
| 1 / ReceiveSubmission | frontend `RequestDTO` → `requestData` | REST/Lambda API exists; explicit adapter needed; requestStatus required by DTO despite sample omitting it | validation; classify / input failure | Partial | `RequestController`, `RequestDTO`, #80 |
| 1 / TranscribeAudio, SuggestCategory | audio/category → text/choice | No verified AWS Transcribe or GenAI contract; frontend decision precedes API | upstream-owned; submit / reject | Blocked | wiki Part 1 |
| 1 / ClassifyContent | subject/description → code 0–3 | Lexicon mock exists; approved AI Lambda contract absent | T, timeout, C; accepted / flagged / failed | Partial | `NotificationEventServiceImpl`, `SentimentCode`, `ai#7` |
| 1 / PersistAccepted or Flagged | classified submission → request ID or review record | `createRequest` persists first and emits notification; bad table absent | idempotency required; created / failure | Blocked | `RequestServiceImpl`, #80, `database#157` |
| 2 / LeadVolunteerChoice | request ID/lead flag → branch | `isLeadVolunteer` and assignment table exist | validate; skip / discovery | Partial | `VolunteerAssignmentService`, wiki Part 2 |
| 2 / MatchInPerson | requester ID, radius/category → candidates | proximity SQL exists, no calamity exclusion zone contract | T, C; candidates / no match | Partial | `VolunteerMatchServiceImpl`, wiki Part 2 |
| 2 / MatchRemote | category → candidates | skill SQL exists | T, C; candidates / no match | Implemented | `VolunteerMatchServiceImpl` |
| 2 / PrepareFirstBatch, PrepareNextBatch | frozen distinct candidate IDs → `{exhausted,number,candidateIds}` | `WorkflowBatchTaskHandler` selects disjoint batches with explicit size; delivery remains #79-owned | T, C; next window / manual review after at most three | Partial | `WorkflowBatchTaskAdapter`, `database#226` |
| 2 / NotifyCandidates | request/candidates → notification event | SQS → dispatch exists, no event dedupe ledger | T, C; wait / failure | Partial | `NotificationEventServiceImpl`, #82 |
| 2 / WaitForAcceptance | request/recipient/task token → accept/decline | No callback store or acceptance API; #79 owns this | multi-day heartbeat/timeout; verify / next batch/manual review | Blocked | #79, wiki Part 2 |
| 2 / VolunteerNotFound | exhausted batches → manual review | No approved status row; `database#226` proposes three 24h batches | bounded retry; manual review terminal | Blocked | `database#226`, #78 |
| 3 / IssueAndDeliverOtp | participant IDs → protected challenge | No approved OTP service/storage; never log/store plaintext | T, expiry; wait / failure | Blocked | wiki Part 3 |
| 3 / VerifyBothParticipants | two validated proofs → verified boolean | No verification service; #79 callback can transport outcome only | attempt/expiry bounds; in progress / reassignment | Blocked | wiki Part 3, #79 |
| 3 / ApproveReassignment | admin decision → approved/cancelled | No admin callback or canonical status | human timeout; match again / cancel | Blocked | wiki Part 3, #79, #78 |
| 4 / AddHelpers | request ID/volunteer IDs → assignments | lead/helper assignment API/table exists | transaction conflicts; continue / error | Partial | `VolunteerAssignmentService`, #84 |
| 4 / DelegateOrganization | organization/request → handoff | No approved organization API/contract | callback/timeout; delegated / failure | Blocked | wiki Part 4 |
| 4 / CompleteWork | verified request ID → resolved | No fulfillment event/API or approved status transition | callback/timeout; resolved / cancellation | Blocked | wiki Part 4, #78 |
| 4 / RequestRatings | resolved request → independent events | Notification pipeline can carry events; rating records/API absent | human timeout; either/both/none terminal | Blocked | wiki Part 4, #74, #79 |
| All / CancelOrDelete | request/execution ID → terminated | existing status endpoints; no execution correlation/StopExecution | idempotent stop; terminal / manual repair | Blocked | `RequestServiceImpl`, #80 |

## Concrete implementation steps and acceptance criteria for this branch

1. Commit a version-controlled Standard Workflow ASL definition with controlled payloads, classification gate, matching branches, callback waits, OTP gate, resolution, and independent rating outcomes. The tasks are explicit contracts; unresolved integrations remain **mocked or blocked**, never silently successful.
2. Provide a Java adapter for the agreed camelCase submission shape and a separate starter boundary with deterministic execution naming. Keep it disconnected from the active Create Request path while #80 persistence ownership and idempotency are unresolved.
3. Provide a local mock harness and automated tests covering good/flagged classification, routing, no match, decline/timeout, OTP gate, cancellation, ratings, and duplicate execution handling. No live AWS credentials.
4. Document Lambda contracts, the exact missing services, AWS Standard Workflow/IAM/SQS/SFN deployment prerequisites, retry and callback behavior, and the rollout sequence. Validate JSON/ASL graph, Maven compile/tests, and `git diff --check`.

Acceptance is **reviewable orchestration and contracts**, not deployed end-to-end functionality. Enabling StartExecution requires #80's decision, an idempotency/outbox mechanism, #78 status mapping, approved AI/OTP/callback services, and DB team migrations. Do not deploy this branch or claim Issue #88 complete before those prerequisites.

## Implementation outcome (appended after coding)

The branch now contains the ASL, a local control-flow simulator and tests, an opt-in deterministic execution starter, a frontend JSON adapter, and a read-only Lambda handler for the existing lexicon/matching services. It also makes `RequestDTO` accept the agreed `additionalFields` and omitted `requestStatus`; persistence of additional fields remains unimplemented. All other Task contracts in the ASL are explicit integration points and require the owners above. The current Create Request path remains active; workflow initiation from it is blocked by #80's conflicting persistence design and missing durable idempotency/outbox.
