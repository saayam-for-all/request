# Part 4 finalization and ratings

Issue #96 extends the PR #91 Standard state machine. The active Create Request API follows its existing notification path. Workflow initiation belongs to [#80](https://github.com/saayam-for-all/request/issues/80).

## Work decisions

Part 4 starts after an approved lead assignment and durable verification of both participants under [#95](https://github.com/saayam-for-all/request/issues/95). The OTP callback carries only verification flags. The lifecycle Lambda must check the stored challenge, request, execution and assignment before confirming `IN_PROGRESS`. The wiki puts `In Progress` before OTP, and [PR #98](https://github.com/saayam-for-all/request/pull/98) proposes updating it at lead assignment. Part 4 requires the verified assignment regardless of any early status update.

`AwaitWorkCompletion` registers a 30-day task token with the internal [#79 callback service](https://github.com/saayam-for-all/request/issues/79). The registry authenticates the lead, validates the action and its parameters, verifies current assignment and execution, and commits the decision before calling `SendTaskSuccess`. A successful callback contains only an action and the opaque reference to the committed decision.

```json
{"action":"ADD_HELPERS","decisionId":"decision-123"}
```

```json
{"operation":"addHelpersIfActive","requestId":"REQ-100","executionId":"example-execution","decisionId":"decision-123"}
```

```json
{"outcome":"HELPERS_ASSIGNED"}
```

Wire the existing state machine's `FinalizationLambdaArn` to `org.sfa.request.stepfunctions.WorkflowFinalizationTaskHandler`. Restrict invocation to the state machine role. The Spring handler requires a `Part4WorkStore` implementation. The store must load decisions in the Request database transaction after the request row lock. It resolves the authenticated actor, current approved assignment and two-party verification, and checks expiry and latest wait ownership. It provides only eligible helpers. An earlier wait, wrong action, expired execution, or unauthenticated actor must yield no authorized command. Replays must return the same current applied decision during the Lambda retry window. Superseded decisions are stale once the next wait opens.

| Action | Successful transition | Other outcomes |
|---|---|---|
| `ADD_HELPERS` | `IN_PROGRESS → IN_PROGRESS`. The existing assignment service adds missing `HELPING` rows as a batch and returns to a fresh work wait. Repeated IDs and existing helpers are idempotent. | Invalid IDs, a lead or conflicting existing assignment, changed assignment, or stale command yield `REJECTED`. |
| `DELEGATE` | Reserved for approved organization handoff. | A valid organization reference yields `DELEGATION_UNAVAILABLE`. The request remains in progress. |
| `COMPLETE` | `IN_PROGRESS → RESOLVED` in one transaction with the applied decision marker. A retry of that committed decision yields `RESOLVED` with the original timestamps. | Missing status approval, ambiguous lookup or unavailable store yield `UNAVAILABLE`. Other states or a different completion after resolution yield `REJECTED`. |
| `CANCEL` | Confirms an already committed `CANCELLED` or `DELETED` request and ends with `TERMINATED`. #79 can also signal `RequestTerminated` after commit. | An active work decision alone yields `REJECTED`. Lead cancellation authority and lifecycle persistence still require approval. |

A verified command for a cancelled/deleted request yields `TERMINATED` before any writes. Rejected or malformed results fail explicitly. Unavailable integrations yield `WorkIntegrationUnavailable`. Work wait timeout queues manual review, using the existing manual review contract. Concurrent request update/cancel/delete/resume and assignment operations in this service use the same request row lock. Other writers and the eventual #79 store must coordinate with that lock and statement ordering.

[#78](https://github.com/saayam-for-all/request/issues/78) and its unmerged [PR #92](https://github.com/saayam-for-all/request/pull/92) document the mismatch among local `COMPLETED`, Database CSV `RESOLVED`, and older DDL comments. The new transaction uses unique semantic `IN_PROGRESS` and `RESOLVED` rows and requires `saayam.workflow.part4.status-writes-approved=true`; it defaults to false. Request and Database teams must verify deployed rows, reconcile legacy callers and any misclassified data, and approve the production write path before setting it. No status IDs or production DDL were changed.

[VolunteerOrg](https://github.com/saayam-for-all/volunteerorg) has an application shell. The [Database organization definitions](https://github.com/saayam-for-all/database/tree/main/ddl/Tables) identify organizations and user mappings. They supply no request handoff, acceptance or completion contract. #96 needs a decision from Request and VolunteerOrg on eligibility, participant permissions, stable handoff keys, acceptance, and ownership of the subsequent lifecycle. Keep organizational state and external delivery gated until those contracts exist.

## Rating invitations and submissions

A committed `RESOLVED` result opens two independent token waits. `Requester` is the existing wire name for the beneficiary side. Determine the permitted principal from approved request-for/guest/self-lead policy and the verified assignment. The persisted submitter ID alone is insufficient for requests made on behalf of someone else. `Volunteer` denotes the verified lead. No additional helper is a rating participant under this contract.

Each `registerRatingCallback` operation passes the request ID, execution ID, purpose, and internal token. `WorkflowRatingCallbacks` expects an approved `Part4RatingStore`. Registration must check committed status and correlation, persist an encrypted token, and stage the matching invitation intent atomically. Use a stable invitation key per execution and role. If a Lambda retry supplies a replacement token, invalidate the old wait and keep the invitation key. The registration Lambda returns promptly while Step Functions holds the wait.

The post-commit relay can call `RatingInvitationPublisher` with the committed intent and a directory-resolved recipient. It builds the `RATING_REQUESTED` event for the existing SQS to SNS/SES transport. The publisher checks the recipient against the intent and uses generic message text. The outbox must claim each intent by its stable key. An SQS send followed by a relay crash can repeat delivery. The current queue and SNS/SES consumers provide no end-to-end deduplication guarantee.

The authenticated submission path resolves the user from the server-side principal. Bind both waits and submissions to the request, execution and role. Score validation uses the Database rating vocabulary (integer 0–5). Database and Product still need to approve mutual rating storage, feedback limits and participant eligibility. The store owns this validation, locks against cancellation and expiry, and commits the independent rating and an acknowledgement intent in one transaction. Retries return a duplicate result without replacing the first rating. An unavailable or failed write leaves the wait open for an eligible retry. The relay acknowledges Step Functions only after commit.

```json
{"outcome":"RECORDED"}
```

AWS task tokens remain internal to the Lambda integration and encrypted registry. Restrict access to Standard execution history and omit execution data from CloudWatch delivery. Rating contents, contact information, OTP codes, and work parameters stay outside Part 4 state input and callback outputs. Standard execution history still contains the earlier workflow's submission data and may include internal task data, so review redaction and history access before activation.

Each branch waits up to **604,800 seconds (seven days)**. The successful workflow returns outcomes in requester-then-volunteer order regardless of response order.

```json
{"requestId":"REQ-100","outcome":"RESOLVED","ratings":[{"outcome":"RECORDED"},{"outcome":"TIMEOUT"}]}
```

A malformed acknowledgement or registration failure produces that role's `ERROR`. A cancelled/deleted request must reject further submissions and signal `RequestTerminated` to every pending wait. A cancelled branch emits `CANCELLED`, and the joined result reports `TERMINATED` with any earlier recorded outcome preserved. The 90-day execution deadline can end the whole run as AWS `TIMED_OUT` before either wait completes. The callback registry must expire tokens at the earlier of the applicable task and execution deadlines. The workflow does not write rating markers over the resolved lifecycle state.

[Database's `volunteer_rating` DDL](https://github.com/saayam-for-all/database/blob/main/ddl/Tables/ddl_volunteer_rating.sql) defines a score, one user ID, and a request ID. There is no rater role, explicit rater/ratee pair, or request-and-role uniqueness constraint. Its request FK names `request_id` while the Request JPA model maps `req_id`. #96 therefore leaves mutual rating persistence and recipient lookup to the Database and #79 contract owners. The unit tests substitute a local store with independent records and acknowledgement intents. Production requires the real store, authenticated callback entry points, encrypted tokens and retry relays. No callback endpoint or database migration is registered here.

## Verification

```bash
mvn -q clean compile
mvn -q -Dtest='Workflow*Test,VolunteerAssignmentServiceImplTest,*Notification*Test,VolunteerMatchRoutingTest' test
python3 docs/step-functions/build_definition.py
python3 -m unittest discover -s docs/step-functions -p test_workflow.py -v
git diff --check
```

Run `WorkflowFinalizationPostgresTest` with `ISSUE96_TEST_JDBC_URL=jdbc:postgresql://127.0.0.1:<port>/request_issue_96` against an empty disposable PostgreSQL database with role `saayam`. It creates a test-only fixture and verifies concurrent helper retries, competing resolution transactions and rollback. Without that variable the integration test is skipped. The unit and ASL suites require no AWS or database.
