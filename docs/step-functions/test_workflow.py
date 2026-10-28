"""Contract tests for the checked-in ASL graph using canned, local task responses."""

import json
import unittest

from build_definition import build
from local_simulator import TaskError, load_definition, run, validate_graph
from render_definition import PLACEHOLDER, render


def submission(request_type=1):
    return {"submissionId": "submission-123", "requestData": {
        "requesterId": "SID-100", "requestSubject": "Calculus help", "requestDescription": "Need tutoring",
        "requestType": {"requestTypeId": request_type}, "helpCategory": {"catId": "4.3.1"},
        "isCalamity": False, "isLeadVolunteer": 0,
    }}


class CannedTasks:
    def __init__(self, **overrides):
        self.calls = []
        self.overrides = overrides
        self.counts = {}

    def __call__(self, state, operation, data):
        self.calls.append(operation)
        self.counts[operation] = self.counts.get(operation, 0) + 1
        if operation in self.overrides:
            replacement = self.overrides[operation]
            outcome = replacement(self.counts[operation]) if callable(replacement) else replacement
            if isinstance(outcome, Exception):
                raise outcome
            return outcome
        defaults = {
            "classify": {"code": 0}, "persistAccepted": {"requestId": "REQ-100"},
            "matchInPerson": {"hasCandidates": True}, "matchRemote": {"hasCandidates": True},
            "firstBatch": {"exhausted": False, "number": 1},
            "registerAcceptanceAndNotify": {"accepted": True, "volunteerId": "VOL-100"},
            "confirmAssignment": {"confirmed": True}, "prepareSelfLead": {"confirmed": True},
            "issueOtp": {"challengeId": "challenge-100"},
            "registerOtpCallback": {"verified": True},
            "markInProgressIfActiveAndVerified": {"updated": True},
            "registerWorkCallback": {"action": "COMPLETE"},
            "resolveIfActive": {"updated": True},
            "registerRatingCallback": {"recorded": True},
            "queueManualReview": {"queued": True},
            "cancelIfActive": {"updated": True},
        }
        if operation == "nextBatch":
            return {"exhausted": self.counts[operation] >= 3,
                    "number": self.counts[operation] + 1}
        return defaults.get(operation, {"updated": True})


class WorkflowTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.machine = load_definition()

    def simulate(self, mock=None, request_type=1):
        return run(self.machine, submission(request_type), mock or CannedTasks())

    def test_definition_is_generated_and_graph_is_closed(self):
        self.assertEqual(build(), self.machine)
        validate_graph(self.machine)
        self.assertGreaterEqual(len(self.machine["States"]), 35)

    def test_render_requires_all_valid_lambda_arns(self):
        source = json.dumps(self.machine)
        keys = set(PLACEHOLDER.findall(source))
        with self.assertRaisesRegex(ValueError, "Missing ARN"):
            render(source, {})
        values = {key: "arn:aws:lambda:us-east-1:000000000000:function:" + key for key in keys}
        rendered = render(source, values)
        self.assertNotIn("${", rendered)
        self.assertEqual(self.machine["StartAt"], json.loads(rendered)["StartAt"])

    def test_good_in_person_request_resolves_and_collects_independent_ratings(self):
        mock = CannedTasks()
        output, path = self.simulate(mock)
        self.assertIn("MatchInPerson", path)
        self.assertIn("MarkInProgress", path)
        self.assertIn("Resolved", path)
        self.assertEqual(2, mock.calls.count("registerRatingCallback"))
        self.assertEqual(2, len(output["ratings"]))

    def test_remote_routes_to_skill_match_only(self):
        mock = CannedTasks()
        _, path = self.simulate(mock, request_type=2)
        self.assertIn("MatchRemote", path)
        self.assertNotIn("MatchInPerson", path)

    def test_flagged_request_never_persists_or_matches(self):
        for code in (1, 2, 3):
            mock = CannedTasks(classify={"code": code})
            _, path = self.simulate(mock)
            self.assertEqual("FlaggedForReview", path[-1])
            self.assertNotIn("persistAccepted", mock.calls)
            self.assertNotIn("matchInPerson", mock.calls)

    def test_malformed_classifier_result_fails_closed(self):
        with self.assertRaisesRegex(TaskError, "MalformedClassification"):
            self.simulate(CannedTasks(classify={"wrong": 0}))

    def test_classifier_transient_error_retries_then_fails(self):
        mock = CannedTasks(classify=lambda _: TaskError("Lambda.ServiceException"))
        with self.assertRaisesRegex(TaskError, "WorkflowFailed"):
            self.simulate(mock)
        self.assertEqual(4, mock.calls.count("classify"))

    def test_classifier_timeout_fails_closed(self):
        mock = CannedTasks(classify=TaskError("States.Timeout"))
        with self.assertRaisesRegex(TaskError, "WorkflowFailed"):
            self.simulate(mock)
        self.assertNotIn("persistAccepted", mock.calls)

    def test_transient_classifier_failure_recovers(self):
        mock = CannedTasks(classify=lambda n: TaskError("Lambda.ServiceException") if n < 3 else {"code": 0})
        _, path = self.simulate(mock)
        self.assertIn("Resolved", path)
        self.assertEqual(3, mock.calls.count("classify"))

    def test_no_volunteer_goes_to_manual_review(self):
        mock = CannedTasks(matchInPerson={"hasCandidates": False})
        _, path = self.simulate(mock)
        self.assertEqual("ManualReviewQueued", path[-1])
        self.assertNotIn("registerAcceptanceAndNotify", mock.calls)

    def test_decline_waits_before_next_batch(self):
        mock = CannedTasks(registerAcceptanceAndNotify={"accepted": False})
        _, path = self.simulate(mock)
        self.assertIn("WaitForNextBatch", path)
        self.assertEqual("ManualReviewQueued", path[-1])

    def test_acceptance_timeout_advances_batch(self):
        mock = CannedTasks(registerAcceptanceAndNotify=TaskError("States.Timeout"))
        _, path = self.simulate(mock)
        self.assertIn("PrepareNextBatch", path)
        self.assertEqual(3, mock.calls.count("registerAcceptanceAndNotify"))
        self.assertEqual("ManualReviewQueued", path[-1])

    def test_invalid_callback_is_rejected(self):
        with self.assertRaisesRegex(TaskError, "MalformedCallback"):
            self.simulate(CannedTasks(registerAcceptanceAndNotify={"volunteerId": "VOL-100"}))

    def test_invalid_otp_cannot_mark_in_progress(self):
        mock = CannedTasks(registerOtpCallback={"verified": False, "attemptsRemaining": False},
                           registerAdminReassignment={"approved": False})
        _, path = self.simulate(mock)
        self.assertIn("AwaitReassignmentApproval", path)
        self.assertNotIn("MarkInProgress", path)
        self.assertEqual("Terminated", path[-1])

    def test_otp_retry_then_valid(self):
        mock = CannedTasks(registerOtpCallback=lambda n: {"verified": False, "attemptsRemaining": True}
                           if n == 1 else {"verified": True})
        _, path = self.simulate(mock)
        self.assertEqual(2, mock.calls.count("issueOtp"))
        self.assertIn("MarkInProgress", path)

    def test_otp_timeout_requires_admin_decision(self):
        mock = CannedTasks(registerOtpCallback=TaskError("States.Timeout"),
                           registerAdminReassignment={"approved": False})
        _, path = self.simulate(mock)
        self.assertIn("AwaitReassignmentApproval", path)
        self.assertNotIn("MarkInProgress", path)

    def test_self_lead_skips_matching(self):
        mock = CannedTasks()
        data = submission()
        data["requestData"]["isLeadVolunteer"] = 1
        _, path = run(self.machine, data, mock)
        self.assertIn("PrepareSelfLead", path)
        self.assertNotIn("MatchInPerson", path)

    def test_helpers_and_delegation_do_not_create_second_matching_path(self):
        mock = CannedTasks(registerWorkCallback=lambda n: {"action": "ADD_HELPERS", "volunteerIds": ["VOL-2"]}
                           if n == 1 else {"action": "DELEGATE", "organizationId": "ORG-1"})
        _, path = self.simulate(mock)
        self.assertIn("AddHelpers", path)
        self.assertEqual("Delegated", path[-1])
        self.assertNotIn("resolveIfActive", mock.calls)

    def test_deleted_or_cancelled_guard_stops_stale_transition(self):
        mock = CannedTasks(markInProgressIfActiveAndVerified={"updated": False, "terminal": True})
        _, path = self.simulate(mock)
        self.assertEqual("Terminated", path[-1])
        self.assertNotIn("registerWorkCallback", mock.calls)

    def test_request_cancelled_during_work(self):
        mock = CannedTasks(registerWorkCallback={"action": "CANCEL"})
        _, path = self.simulate(mock)
        self.assertEqual("Terminated", path[-1])
        self.assertNotIn("resolveIfActive", mock.calls)

    def test_independent_rating_timeout(self):
        mock = CannedTasks(registerRatingCallback=TaskError("States.Timeout"))
        output, path = self.simulate(mock)
        self.assertEqual("Resolved", path[-1])
        self.assertEqual([{"outcome": "TIMEOUT"}, {"outcome": "TIMEOUT"}], output["ratings"])


if __name__ == "__main__":
    unittest.main()
