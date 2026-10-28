"""Render ASL Lambda ARN placeholders to a temporary deployment file.

The required values are read from same-named environment variables. This script
does not deploy anything or change the source definition.
"""

import argparse
import json
import os
import re

from local_simulator import DEFINITION, validate_graph


PLACEHOLDER = re.compile(r"\$\{([A-Za-z][A-Za-z0-9]*)\}")
LAMBDA_ARN = re.compile(r"^arn:aws[a-z-]*:lambda:[a-z0-9-]+:[0-9]{12}:function:[A-Za-z0-9_-]+(?::[A-Za-z0-9_-]+)?$")


def render(source, values):
    required = set(PLACEHOLDER.findall(source))
    missing = required - values.keys()
    if missing:
        raise ValueError("Missing ARN variables: " + ", ".join(sorted(missing)))
    for key in required:
        if not LAMBDA_ARN.fullmatch(values[key]):
            raise ValueError("Invalid Lambda ARN variable: " + key)
    output = PLACEHOLDER.sub(lambda match: values[match.group(1)], source)
    validate_graph(json.loads(output))
    return output


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", required=True, help="Path outside the source tree for rendered ASL")
    args = parser.parse_args()
    definition = DEFINITION.read_text()
    keys = set(PLACEHOLDER.findall(definition))
    rendered = render(definition, {key: os.environ[key] for key in keys if key in os.environ})
    with open(args.output, "w", encoding="utf-8") as handle:
        handle.write(rendered)
    print(args.output)
