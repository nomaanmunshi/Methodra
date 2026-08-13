# Build and verification status

Prepared: 2026-08-13.

## Executed while preparing this repository

- `python3 scripts/validate_catalog.py` — passed: 10 methods, 15 rules, 4 book collections; Android/backend copies byte-identical.
- Dependency-free Kotlin domain compilation — passed for models, deterministic matcher, schedule evaluator and protocol progression/recovery logic.
- Dependency-free Java compilation — passed for backend method records and token codec.
- JSON, XML and YAML files were parsed during static packaging checks.

## Not executed in the packaging environment

A full Gradle Android build and Spring Boot dependency-resolved test run could not be executed because the packaging runtime did not have outbound dependency resolution/Android SDK access.

That limitation is why the repository includes GitHub Actions. CI is expected to be the first authoritative dependency-resolved build. A failed CI run should be treated as a real defect to fix; do not replace it with a badge or claim that tests passed.

## Release posture

The tag workflow builds a debug-signed portfolio APK and backend JAR. Production Play signing, Play Console declarations, real-device focus-policy verification, and production hosting are intentionally outside this archive.
