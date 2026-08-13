# Contributing to Methodra

Methodra is deliberately small in Version 1. Contributions should improve reliability, accessibility, evidence quality, or an existing protocol before adding scope.

## Before opening a pull request

1. Keep matching deterministic. Do not add generative recommendations to V1.
2. Add or change evidence claims only with a source, a plain-language limitation, and conservative wording.
3. Keep Android usage details local by default.
4. Do not copy book prose, diagrams, commercial app assets, or branded exercises.
5. Run `python3 scripts/validate_catalog.py`, backend tests, and Android unit tests.
6. Add tests for behavior-changing code.

## Architecture

The Android application remains a single app module until boundaries become costly enough to justify Gradle modularization. The backend is a Spring Boot modular monolith. Do not introduce microservices for V1.

## Commit style

Use short imperative commits such as `Add recurring focus schedule evaluator` or `Validate evidence limitations at startup`.
