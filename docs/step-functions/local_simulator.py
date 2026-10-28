"""Small deterministic ASL path simulator for local contract tests; it does not emulate AWS."""

import copy
import json
from pathlib import Path


DEFINITION = Path(__file__).resolve().parents[2] / "src/main/resources/stepfunctions/request-workflow.asl.json"


class TaskError(Exception):
    def __init__(self, code):
        super().__init__(code)
        self.code = code


def get_path(data, path):
    value = data
    for part in path.removeprefix("$.").split("."):
        if not isinstance(value, dict) or part not in value:
            return None
        value = value[part]
    return value


def put_path(data, path, value):
    current = data
    parts = path.removeprefix("$.").split(".")
    for part in parts[:-1]:
        current = current.setdefault(part, {})
    current[parts[-1]] = value


def matches(rule, data):
    value = get_path(data, rule["Variable"])
    for comparator in ("NumericEquals", "BooleanEquals", "StringEquals"):
        if comparator in rule:
            return type(value) is type(rule[comparator]) and value == rule[comparator]
    if "IsString" in rule:
        return isinstance(value, str) == rule["IsString"]
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


def run(machine, submission, mock, max_steps=100):
    visited = []

    def execute(scope, initial):
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
                        if not state["Resource"].endswith(".waitForTaskToken"):
                            value = {"Payload": value}
                        put_path(data, state["ResultPath"], value)
                        break
                    except TaskError as error:
                        retry = next((x for x in state["Retry"] if error.code in x["ErrorEquals"]), None)
                        if retry and attempts < retry["MaxAttempts"]:
                            attempts += 1
                            continue
                        catcher = next((x for x in state["Catch"]
                                        if error.code in x["ErrorEquals"] or "States.ALL" in x["ErrorEquals"]), None)
                        if not catcher:
                            raise
                        if "ResultPath" in catcher:
                            put_path(data, catcher["ResultPath"], {"Error": error.code})
                        name = catcher["Next"]
                        caught = True
                        break
                if caught:
                    continue
                name = state.get("Next")
                if name is None:
                    return data
                continue
            if kind == "Parallel":
                put_path(data, state["ResultPath"], [execute(branch, data) for branch in state["Branches"]])
                name = state["Next"]
                continue
            if kind == "Wait":
                name = state["Next"]
                continue
            if kind == "Pass":
                if "Result" in state:
                    data = state["Result"]
                if state.get("End"):
                    return data
                name = state["Next"]
                continue
            if kind == "Succeed":
                return data
            if kind == "Fail":
                raise TaskError(state["Error"])
            raise AssertionError(f"Unsupported state: {kind}")

    output = execute(machine, submission)
    return output, visited


def load_definition():
    return json.loads(DEFINITION.read_text())
