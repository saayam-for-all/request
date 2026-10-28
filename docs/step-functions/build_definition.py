"""Build the checked-in JSONPath ASL definition. Run from any directory."""

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "src/main/resources/stepfunctions/request-workflow.asl.json"
TRANSIENT = ["Lambda.ServiceException", "Lambda.AWSLambdaException", "Lambda.SdkClientException"]


def task(operation, function, next_state=None, result_path=None, payload=None, callback=False, timeout=30, catches=None):
    body = {
        "Type": "Task",
        "Resource": "arn:aws:states:::lambda:invoke.waitForTaskToken" if callback else "arn:aws:states:::lambda:invoke",
        "Parameters": {
            "FunctionName": "${" + function + "}",
            "Payload": {"operation": operation, "executionId.$": "$$.Execution.Id", **(payload or {})},
        },
        "ResultPath": result_path,
        "TimeoutSeconds": timeout,
        "Retry": [{"ErrorEquals": TRANSIENT, "IntervalSeconds": 2, "BackoffRate": 2, "MaxAttempts": 3}],
        "Catch": catches if catches is not None else [{"ErrorEquals": ["States.ALL"], "ResultPath": "$.error", "Next": "WorkflowFailed"}],
    }
    if callback:
        body["Parameters"]["Payload"]["taskToken.$"] = "$$.Task.Token"
    if next_state:
        body["Next"] = next_state
    else:
        body["End"] = True
    return body


def choice(rules, default):
    return {"Type": "Choice", "Choices": rules, "Default": default}


def rule(path, comparator, value, next_state):
    return {"Variable": path, comparator: value, "Next": next_state}


def rating_branch(purpose):
    name = "Await" + purpose + "Rating"
    recorded = purpose + "RatingRecorded"
    timed_out = purpose + "RatingTimedOut"
    failed = purpose + "RatingFailed"
    return {
        "StartAt": name,
        "States": {
            name: task(
                "registerRatingCallback", "CallbackRegistrationLambdaArn", recorded, "$.rating",
                {"requestId.$": "$.persistedInvoke.Payload.requestId", "purpose": purpose},
                callback=True, timeout=604800,
                catches=[
                    {"ErrorEquals": ["States.Timeout"], "Next": timed_out},
                    {"ErrorEquals": ["States.ALL"], "Next": failed},
                ],
            ),
            recorded: {"Type": "Succeed"},
            timed_out: {"Type": "Pass", "Result": {"outcome": "TIMEOUT"}, "End": True},
            failed: {"Type": "Pass", "Result": {"outcome": "ERROR"}, "End": True},
        },
    }


def build():
    s = {}
    s["ValidateSubmission"] = choice([
        rule("$.submissionId", "IsString", True, "CheckRequestData")
    ], "InvalidSubmission")
    s["CheckRequestData"] = choice([
        rule("$.requestData.requestSubject", "IsString", True, "ClassifyContent")
    ], "InvalidSubmission")
    s["ClassifyContent"] = task("classify", "ClassifierLambdaArn", "ClassificationDecision", "$.classificationInvoke", {
        "submissionId.$": "$.submissionId", "subject.$": "$.requestData.requestSubject",
        "description.$": "$.requestData.requestDescription"}, timeout=20)
    s["ClassificationDecision"] = choice([
        rule("$.classificationInvoke.Payload.code", "NumericEquals", 0, "PersistAccepted"),
        rule("$.classificationInvoke.Payload.code", "NumericEquals", 1, "FlaggedForReview"),
        rule("$.classificationInvoke.Payload.code", "NumericEquals", 2, "FlaggedForReview"),
        rule("$.classificationInvoke.Payload.code", "NumericEquals", 3, "FlaggedForReview"),
    ], "MalformedClassification")
    s["PersistAccepted"] = task("persistAccepted", "PersistenceLambdaArn", "CheckPersistence", "$.persistedInvoke", {
        "submissionId.$": "$.submissionId", "requestData.$": "$.requestData",
        "classificationCode.$": "$.classificationInvoke.Payload.code"})
    s["CheckPersistence"] = choice([
        rule("$.persistedInvoke.Payload.requestId", "IsString", True, "CheckLeadVolunteer")
    ], "MalformedPersistence")
    s["CheckLeadVolunteer"] = choice([
        rule("$.requestData.isLeadVolunteer", "NumericEquals", 1, "PrepareSelfLead")
    ], "RouteMatching")
    s["PrepareSelfLead"] = task("prepareSelfLead", "AssignmentLambdaArn", "CheckSelfAssignment", "$.assignmentInvoke", {
        "requestId.$": "$.persistedInvoke.Payload.requestId", "requesterId.$": "$.requestData.requesterId"})
    s["CheckSelfAssignment"] = choice([
        rule("$.assignmentInvoke.Payload.confirmed", "BooleanEquals", True, "IssueOtp")
    ], "ManualReview")
    s["RouteMatching"] = choice([
        rule("$.requestData.requestType.requestTypeId", "NumericEquals", 1, "MatchInPerson"),
        rule("$.requestData.requestType.requestTypeId", "NumericEquals", 2, "MatchRemote"),
    ], "InvalidRequestType")
    for name, operation in [("MatchInPerson", "matchInPerson"), ("MatchRemote", "matchRemote")]:
        s[name] = task(operation, "MatchingLambdaArn", "CheckMatches", "$.matchInvoke", {
            "requestId.$": "$.persistedInvoke.Payload.requestId",
            "requesterId.$": "$.requestData.requesterId",
            "categoryId.$": "$.requestData.helpCategory.catId",
            "isCalamity.$": "$.requestData.isCalamity"})
    s["CheckMatches"] = choice([
        rule("$.matchInvoke.Payload.hasCandidates", "BooleanEquals", True, "PrepareFirstBatch"),
        rule("$.matchInvoke.Payload.hasCandidates", "BooleanEquals", False, "ManualReview"),
    ], "MalformedMatch")
    s["PrepareFirstBatch"] = task("firstBatch", "BatchLambdaArn", "BatchDecision", "$.batchInvoke", {
        "requestId.$": "$.persistedInvoke.Payload.requestId", "match.$": "$.matchInvoke.Payload"})
    s["PrepareNextBatch"] = task("nextBatch", "BatchLambdaArn", "BatchDecision", "$.batchInvoke", {
        "requestId.$": "$.persistedInvoke.Payload.requestId", "previous.$": "$.batchInvoke.Payload",
        "match.$": "$.matchInvoke.Payload"})
    s["BatchDecision"] = choice([
        rule("$.batchInvoke.Payload.exhausted", "BooleanEquals", True, "ManualReview"),
        rule("$.batchInvoke.Payload.exhausted", "BooleanEquals", False, "AwaitAcceptance"),
    ], "MalformedBatch")
    s["AwaitAcceptance"] = task("registerAcceptanceAndNotify", "CallbackRegistrationLambdaArn", "AcceptanceDecision", "$.acceptance", {
        "requestId.$": "$.persistedInvoke.Payload.requestId", "batch.$": "$.batchInvoke.Payload"},
        callback=True, timeout=86400,
        catches=[
            {"ErrorEquals": ["States.Timeout"], "Next": "PrepareNextBatch"},
            {"ErrorEquals": ["States.ALL"], "ResultPath": "$.error", "Next": "WorkflowFailed"},
        ])
    s["AcceptanceDecision"] = choice([
        rule("$.acceptance.accepted", "BooleanEquals", True, "ConfirmAssignment"),
        rule("$.acceptance.accepted", "BooleanEquals", False, "WaitForNextBatch"),
    ], "MalformedCallback")
    s["WaitForNextBatch"] = {"Type": "Wait", "Seconds": 86400, "Next": "PrepareNextBatch"}
    s["ConfirmAssignment"] = task("confirmAssignment", "AssignmentLambdaArn", "CheckAssignment", "$.assignmentInvoke", {
        "requestId.$": "$.persistedInvoke.Payload.requestId", "volunteerId.$": "$.acceptance.volunteerId"})
    s["CheckAssignment"] = choice([
        rule("$.assignmentInvoke.Payload.confirmed", "BooleanEquals", True, "IssueOtp"),
        rule("$.assignmentInvoke.Payload.confirmed", "BooleanEquals", False, "PrepareNextBatch"),
    ], "MalformedAssignment")
    s["IssueOtp"] = task("issueOtp", "OtpLambdaArn", "AwaitOtpVerification", "$.otpInvoke", {
        "requestId.$": "$.persistedInvoke.Payload.requestId", "assignment.$": "$.assignmentInvoke.Payload"})
    s["AwaitOtpVerification"] = task("registerOtpCallback", "CallbackRegistrationLambdaArn", "OtpDecision", "$.otpVerification", {
        "requestId.$": "$.persistedInvoke.Payload.requestId", "challengeId.$": "$.otpInvoke.Payload.challengeId"},
        callback=True, timeout=900,
        catches=[
            {"ErrorEquals": ["States.Timeout"], "Next": "AwaitReassignmentApproval"},
            {"ErrorEquals": ["States.ALL"], "ResultPath": "$.error", "Next": "WorkflowFailed"},
        ])
    s["OtpDecision"] = choice([
        rule("$.otpVerification.verified", "BooleanEquals", True, "MarkInProgress"),
        rule("$.otpVerification.attemptsRemaining", "BooleanEquals", True, "IssueOtp"),
    ], "AwaitReassignmentApproval")
    s["AwaitReassignmentApproval"] = task("registerAdminReassignment", "CallbackRegistrationLambdaArn", "ReassignmentDecision", "$.reassignment", {
        "requestId.$": "$.persistedInvoke.Payload.requestId"}, callback=True, timeout=604800,
        catches=[
            {"ErrorEquals": ["States.Timeout"], "Next": "ManualReview"},
            {"ErrorEquals": ["States.ALL"], "ResultPath": "$.error", "Next": "WorkflowFailed"},
        ])
    s["ReassignmentDecision"] = choice([
        rule("$.reassignment.approved", "BooleanEquals", True, "RouteMatching"),
        rule("$.reassignment.approved", "BooleanEquals", False, "CancelByAdmin"),
    ], "MalformedCallback")
    s["MarkInProgress"] = task("markInProgressIfActiveAndVerified", "LifecycleLambdaArn", "CheckInProgress", "$.progressInvoke", {
        "requestId.$": "$.persistedInvoke.Payload.requestId", "challengeId.$": "$.otpInvoke.Payload.challengeId",
        "verification.$": "$.otpVerification"})
    s["CheckInProgress"] = choice([
        rule("$.progressInvoke.Payload.updated", "BooleanEquals", True, "AwaitWorkCompletion"),
        rule("$.progressInvoke.Payload.terminal", "BooleanEquals", True, "Terminated"),
    ], "MalformedTransition")
    s["AwaitWorkCompletion"] = task("registerWorkCallback", "CallbackRegistrationLambdaArn", "WorkDecision", "$.work", {
        "requestId.$": "$.persistedInvoke.Payload.requestId"}, callback=True, timeout=2592000,
        catches=[
            {"ErrorEquals": ["States.Timeout"], "Next": "ManualReview"},
            {"ErrorEquals": ["States.ALL"], "ResultPath": "$.error", "Next": "WorkflowFailed"},
        ])
    s["WorkDecision"] = choice([
        rule("$.work.action", "StringEquals", "ADD_HELPERS", "AddHelpers"),
        rule("$.work.action", "StringEquals", "DELEGATE", "DelegateOrganization"),
        rule("$.work.action", "StringEquals", "COMPLETE", "MarkResolved"),
        rule("$.work.action", "StringEquals", "CANCEL", "CancelByAdmin"),
    ], "MalformedCallback")
    s["AddHelpers"] = task("addHelpersIfActive", "AssignmentLambdaArn", "AwaitWorkCompletion", "$.helpersInvoke", {
        "requestId.$": "$.persistedInvoke.Payload.requestId", "volunteerIds.$": "$.work.volunteerIds"})
    s["DelegateOrganization"] = task("delegateIfActive", "DelegationLambdaArn", "Delegated", "$.delegationInvoke", {
        "requestId.$": "$.persistedInvoke.Payload.requestId", "organizationId.$": "$.work.organizationId"})
    s["MarkResolved"] = task("resolveIfActive", "LifecycleLambdaArn", "CheckResolved", "$.resolutionInvoke", {
        "requestId.$": "$.persistedInvoke.Payload.requestId"})
    s["CheckResolved"] = choice([
        rule("$.resolutionInvoke.Payload.updated", "BooleanEquals", True, "CollectRatings"),
        rule("$.resolutionInvoke.Payload.terminal", "BooleanEquals", True, "Terminated"),
    ], "MalformedTransition")
    s["CollectRatings"] = {"Type": "Parallel", "Branches": [rating_branch("Requester"), rating_branch("Volunteer")],
                           "ResultPath": "$.ratings", "Next": "Resolved"}
    s["CancelByAdmin"] = task("cancelIfActive", "LifecycleLambdaArn", "Terminated", "$.cancelInvoke", {
        "requestId.$": "$.persistedInvoke.Payload.requestId"})
    s["ManualReview"] = task("queueManualReview", "ManualReviewLambdaArn", "ManualReviewQueued", "$.reviewInvoke", {
        "requestId.$": "$.persistedInvoke.Payload.requestId"})
    for name in ["FlaggedForReview", "ManualReviewQueued", "Terminated", "Delegated", "Resolved"]:
        s[name] = {"Type": "Succeed"}
    for name in ["InvalidSubmission", "MalformedClassification", "MalformedPersistence", "InvalidRequestType",
                 "MalformedMatch", "MalformedBatch", "MalformedCallback", "MalformedAssignment", "MalformedTransition",
                 "WorkflowFailed"]:
        s[name] = {"Type": "Fail", "Error": name, "Cause": "Workflow input, task output, or dependency failed"}
    return {"Comment": "Issue 88 verified-diagram contract; blocked integrations are explicit", "StartAt": "ValidateSubmission",
            "TimeoutSeconds": 7776000, "States": s}


if __name__ == "__main__":
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(json.dumps(build(), indent=2) + "\n")
    print(OUTPUT)
