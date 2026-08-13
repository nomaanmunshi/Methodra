#!/usr/bin/env python3
"""Dependency-free validation for Methodra catalog/configuration parity."""
from __future__ import annotations

import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "android/app/src/main/assets"
BACKEND = ROOT / "backend/src/main/resources"


def load(base: Path, name: str):
    with (base / name).open(encoding="utf-8") as fh:
        return json.load(fh)


def fail(message: str) -> None:
    print(f"ERROR: {message}", file=sys.stderr)
    raise SystemExit(1)


def main() -> None:
    names = ["methods.json", "method-rules.json", "book-collections.json"]
    for name in names:
        if (ANDROID / name).read_bytes() != (BACKEND / name).read_bytes():
            fail(f"Android/backend catalog copies differ: {name}")

    methods = load(ANDROID, "methods.json")
    rules = load(ANDROID, "method-rules.json")
    collections = load(ANDROID, "book-collections.json")

    if len(methods) != 10:
        fail(f"Expected exactly 10 V1 methods, found {len(methods)}")
    if len(collections) != 4:
        fail(f"Expected exactly 4 V1 book collections, found {len(collections)}")

    method_ids = [m.get("id") for m in methods]
    if len(set(method_ids)) != len(method_ids):
        fail("Duplicate method IDs")

    required_method_fields = {
        "id", "name", "shortExplanation", "intendedProblem", "mayHelpWhen", "unsuitableWhen",
        "evidence", "inspiration", "setupQuestions", "steps", "minimumVersion", "focusRule",
        "outcomeMetric", "reviewDays", "stopConditions", "sourceLabels", "sourceUrls", "goalDomains", "obstacles"
    }
    for method in methods:
        missing = required_method_fields - set(method)
        if missing:
            fail(f"{method.get('id')} missing fields: {sorted(missing)}")
        evidence = method["evidence"]
        if evidence.get("level") not in {"A", "B", "C", "D"}:
            fail(f"Invalid evidence level for {method['id']}")
        if not evidence.get("limitation", "").strip():
            fail(f"Missing evidence limitation for {method['id']}")
        if not method["sourceLabels"]:
            fail(f"Missing source labels for {method['id']}")
        if len(method["sourceUrls"]) != len(method["sourceLabels"]):
            fail(f"Source label/URL count mismatch for {method['id']}")
        if any(str(url).strip() and not str(url).startswith(("https://", "http://")) for url in method["sourceUrls"]):
            fail(f"Invalid source URL for {method['id']}")
        orders = [step.get("order") for step in method["steps"]]
        if orders != list(range(1, len(orders) + 1)):
            fail(f"Protocol step order is not contiguous for {method['id']}")

    rule_ids = set()
    allowed_fields = {"goalDomain", "obstacle", "highScreenTime", "structureLevel", "availableMinutes"}
    allowed_ops = {"eq", "in", "lte", "gte"}
    for rule in rules:
        if rule.get("id") in rule_ids:
            fail(f"Duplicate rule ID: {rule.get('id')}")
        rule_ids.add(rule.get("id"))
        if rule.get("methodId") not in method_ids:
            fail(f"Rule points to unknown method: {rule.get('id')}")
        if rule.get("field") not in allowed_fields:
            fail(f"Unsupported rule field: {rule.get('field')}")
        if rule.get("operator") not in allowed_ops:
            fail(f"Unsupported rule operator: {rule.get('operator')}")
        if not isinstance(rule.get("score"), int) or not (1 <= rule["score"] <= 100):
            fail(f"Invalid rule score: {rule.get('id')}")
        if not str(rule.get("reason", "")).strip():
            fail(f"Rule has no transparent reason: {rule.get('id')}")

    collection_ids = [c.get("id") for c in collections]
    if len(set(collection_ids)) != len(collection_ids):
        fail("Duplicate book collection IDs")
    for collection in collections:
        if collection.get("evidenceLevel") not in {"C", "D"}:
            fail(f"Book collection should remain C/D without separate evidence: {collection.get('id')}")
        if not collection.get("evidenceNote", "").strip():
            fail(f"Missing book collection evidence note: {collection.get('id')}")

    print(f"OK: {len(methods)} methods, {len(rules)} matching rules, {len(collections)} book collections")
    print("OK: Android/backend catalog assets are byte-identical")


if __name__ == "__main__":
    main()
