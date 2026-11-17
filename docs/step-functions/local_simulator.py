"""Small deterministic ASL path simulator for local contract tests; it does not emulate AWS."""

import copy
import json
import re
from dataclasses import dataclass
from pathlib import Path


DEFINITION = Path(__file__).resolve().parents[2] / "src/main/resources/stepfunctions/request-workflow.asl.json"


class TaskError(Exception):
    def __init__(self, code):
        super().__init__(code)
        self.code = code


def get_path(data, path):
    value = data
    if path == "$":
        return value
    for part in re.split(r"\.|\[|\]", path.removeprefix("$.")):
        if not part:
            continue
        if isinstance(value, list) and part.isdigit() and int(part) < len(value):
            value = value[int(part)]
        elif isinstance(value, dict) and part in value:
            value = value[part]
        else:
            return None
    return value


def parameters(specification, data):
    return {key.removesuffix(".$"): get_path(data, value) if key.endswith(".$") else value
            for key, value in specification.items()}


@dataclass(frozen=True)
class After:
    seconds: int
    response: object


def put_path(data, path, value):
    current = data
    parts = path.removeprefix("$.").split(".")
    for part in parts[:-1]:
        current = current.setdefault(part, {})
    current[parts[-1]] = value


def matches(rule, data):
    if "And" in rule:
        return all(matches(condition, data) for condition in rule["And"])
    value = get_path(data, rule["Variable"])
    for comparator in ("NumericEquals", "BooleanEquals", "StringEquals"):
        if comparator in rule:
            return type(value) is type(rule[comparator]) and value == rule[comparator]
    for comparator, expected_type in [("IsString", str), ("IsBoolean", bool)]:
        if comparator in rule:
            return isinstance(value, expected_type) == rule[comparator]
    raise AssertionError(f"Unsupported Choice rule: {rule}")


def validate_graph(machine):
    """Check graph references and required callback/retry guards, not AWS syntax."""
    def check(scope):
        states = scope["States"]
        assert scope["StartAt"] in states
        for name, state in states.items():
            destinations = [state[k] for k in ("Next", "Default") if k in state]
            destinations += [x["Next"] for x in state.get("Choices", [])]
            destinations += [x["Next"] for x in state.get("Catch", [])]
            assert all(destination in states for destination in destinations), name
            if state["Type"] == "Task":
                assert state["TimeoutSeconds"] > 0 and state["Retry"] and state["Catch"], name
                if state["Resource"].endswith(".waitForTaskToken"):
                    assert state["Parameters"]["Payload"]["taskToken.$"] == "$$.Task.Token", name
            if state["Type"] == "Parallel":
                for branch in state["Branches"]:
                    check(branch)
    check(machine)


def run(machine, submission, mock, max_steps=100, completions=None):
    visited = []

    deadline = machine.get("TimeoutSeconds", float("inf"))

    def execute(scope, initial, elapsed):
        def advance(seconds):
            nonlocal elapsed
            elapsed += seconds
            if elapsed >= deadline:
                raise TaskError("ExecutionTimedOut")

        data = copy.deepcopy(initial)
        name = scope["StartAt"]
        while True:
            visited.append(name)
            if len(visited) > max_steps:
                raise AssertionError("Workflow did not terminate")
            state = scope["States"][name]
            kind = state["Type"]
            if kind == "Choice":
                name = next((rule["Next"] for rule in state["Choices"] if matches(rule, data)), state["Default"])
                continue
            if kind == "Task":
                operation = state["Parameters"]["Payload"]["operation"]
                attempts = 0
                caught = False
                while True:
                    try:
                        value = mock(name, operation, data)
                        if isinstance(value, After):
                            advance(min(value.seconds, state["TimeoutSeconds"]))
                            if value.seconds >= state["TimeoutSeconds"]:
                                raise TaskError("States.Timeout")
                            value = value.response
                        if isinstance(value, TaskError):
                            raise value
                        if completions is not None:
                            completions.append((elapsed, name))
                        if not state["Resource"].endswith(".waitForTaskToken"):
                            value = {"Payload": value}
                        result_path = state.get("ResultPath", "$")
                        if result_path == "$":
                            data = value
                        elif result_path is not None:
                            put_path(data, result_path, value)
                        break
                    except TaskError as error:
                        if error.code == "ExecutionTimedOut":
                            raise
                        retry = next((x for x in state["Retry"] if error.code in x["ErrorEquals"]), None)
                        if retry and attempts < retry["MaxAttempts"]:
                            advance(retry["IntervalSeconds"] * retry.get("BackoffRate", 2) ** attempts)
                            attempts += 1
                            continue
                        catcher = next((x for x in state["Catch"]
                                        if error.code in x["ErrorEquals"] or "States.ALL" in x["ErrorEquals"]), None)
                        if not catcher:
                            raise
                        error_path = catcher.get("ResultPath", "$")
                        if error_path == "$":
                            data = {"Error": error.code}
                        elif error_path is not None:
                            put_path(data, error_path, {"Error": error.code})
                        name = catcher["Next"]
                        caught = True
                        break
                if caught:
                    continue
                name = state.get("Next")
                if name is None:
                    return data, elapsed
                continue
            if kind == "Parallel":
                branch_results = [execute(branch, data, elapsed) for branch in state["Branches"]]
                put_path(data, state["ResultPath"], [result for result, _ in branch_results])
                elapsed = max(seconds for _, seconds in branch_results)
                name = state["Next"]
                continue
            if kind == "Wait":
                advance(state["Seconds"])
                name = state["Next"]
                continue
            if kind == "Pass":
                if "Result" in state:
                    data = state["Result"]
                elif "Parameters" in state:
                    data = parameters(state["Parameters"], data)
                if state.get("End"):
                    return data, elapsed
                name = state["Next"]
                continue
            if kind == "Succeed":
                return data, elapsed
            if kind == "Fail":
                raise TaskError(state["Error"])
            raise AssertionError(f"Unsupported state: {kind}")

    output, _ = execute(machine, submission, 0)
    return output, visited


def load_definition():
    return json.loads(DEFINITION.read_text())
