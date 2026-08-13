# Architecture

## System shape

Methodra is deliberately two deployables:

1. **Android application** — owns daily execution, focus rules, usage access, personal trials and offline-first state.
2. **Spring Boot backend** — optional account/sync boundary plus a server-side copy of the catalog and deterministic matcher.

The backend is a **modular monolith**, not microservices.

## Android

The Android project stays in one Gradle app module for V1. Package boundaries carry the architecture without adding premature build complexity:

```text
io.methodra.app
├── data/local          Room entities + DAOs
├── data/repository     local-first repositories and catalog loaders
├── design              design tokens/theme
├── di                  Hilt bindings
├── domain              pure models, matcher and schedule evaluator
├── onboarding          stone + assessment + setup
├── today               daily protocol execution/recovery
├── methods             catalog/evidence UI
├── focus               UsageStats, schedules, AccessibilityService protection
├── lab                 personal trial/review
├── settings            accessibility/privacy/export/delete
└── ui                  shell/navigation/view models
```

### State ownership

- **Room**: protocols, daily states, focus rules/sessions/schedules, trials and trial entries.
- **DataStore**: onboarding completion, reduced motion and haptic preference.
- **SharedPreferences mirror**: only the minimal fast-read focus guard configuration required by the platform AccessibilityService. Room remains the durable history source.
- **UsageStatsManager**: read on demand; detailed usage events are not copied into backend storage.

### Deterministic Method Engine

`methods.json` defines content. `method-rules.json` defines validated scoring rules. The engine supports a narrow typed rule vocabulary (`eq`, `in`, `lte`, `gte`) over known input fields. There are no executable database scripts and no generative model in the recommendation path.

Tie-breaking is deterministic: score descending, method ID ascending. At most three results are returned, each with visible reasons.

### Focus protection

Normal local usage summaries require Usage Access. Stronger best-effort intervention is isolated in `MethodraAccessibilityService` and only handles `TYPE_WINDOW_STATE_CHANGED`. The service configuration explicitly sets `canRetrieveWindowContent=false`.

Protection can be active because of a live focus session, a recurring local-time schedule, or a user-selected daily budget. Emergency/session exits and schedule editing remain available. The implementation never claims tamper resistance.

## Backend

```text
io.methodra.backend
├── api          REST controllers + problem responses
├── auth         users, opaque API tokens, hashing
├── methods      catalog + deterministic matcher
├── security     stateless bearer authentication
└── sync         versioned client document synchronization
```

PostgreSQL schema changes are managed by Flyway. Sync uses an explicit client `baseVersion` plus JPA `@Version`; a stale client receives a conflict instead of silently overwriting newer server state.

## Catalog parity

The Android and backend catalog JSON files are intentionally duplicated because each deployable must work independently. `scripts/validate_catalog.py` fails CI unless the copies are byte-identical and structurally valid.

## Deliberate omissions

No AI coach, payments, social graph, public leaderboard, financial commitment, iOS client, wearable integration, calorie tracking, meditation generation, or microservices are in V1.
