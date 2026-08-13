# Methodra

**Methods that survive real life.**

Methodra is an open-source, method-driven Android digital-wellbeing project. It converts a concrete outcome and obstacle into an explainable protocol, protects execution time, records small evidence, and asks whether the protocol should continue, simplify, change, or stop.

It is intentionally **not** a generic to-do list, motivational quote app, AI coach, social network, or streak game.

## Product loop

`Goal → obstacle → matched method → setup → daily protocol → protected execution → evidence → review`

The primary object is a **Method**, not a task. Version 1 uses a deterministic rules engine; it never pretends an LLM knows what the user needs.

## What is in this repository

### Android client

- Kotlin + Jetpack Compose + Material 3
- Original eight-stage tap-to-fracture onboarding with skip, haptic and reduced-motion paths
- Short assessment and deterministic top-three Method Engine recommendations
- Required setup questions before protocol activation
- One active protocol at a time
- Today screen with up to three protocol actions, match reasons, difficult-day version and check-in
- Separate automaticity rating for Context Anchor; no streak-reset mechanic
- Recovery classification: ability, opportunity, motivation, task size, timing, or unrealistic rule
- Ten Version-1 methods with evidence level, limitation, protocol, stop conditions and source links
- Exactly four Version-1 book-inspired collections: Deep Work, Psycho-Cybernetics, The Pragmatic Programmer, and Make It Stick
- Focus timer, selected-app rules, daily budgets, recurring schedules and emergency exit
- Local usage summaries through `UsageStatsManager` after explicit permission
- Optional AccessibilityService protection that observes foreground-package changes only; window content retrieval is disabled
- Personal Lab with one primary metric and conservative association language
- Room + DataStore local persistence, JSON export and local deletion

### Java backend

- Java 21 + Spring Boot modular monolith
- PostgreSQL + Flyway
- Public method catalog and deterministic matching endpoints
- Public four-collection endpoint
- Optional email/password account creation and opaque bearer tokens
- BCrypt password hashing and server-side SHA-256 token hashes
- Versioned sync-document endpoint with conflict detection and JPA optimistic locking
- Bean Validation, Spring Security and Actuator health endpoint
- Unit tests and PostgreSQL integration test through Testcontainers
- Dockerfile and Docker Compose

## Repository map

```text
Methodra/
├── .github/workflows/       # CI and tag-based demo releases
├── android/                 # Native Android application
├── backend/                 # Java 21 Spring Boot backend
├── docs/                    # architecture, privacy, evidence, API and threat model
├── scripts/                 # dependency-free catalog validation
├── docker-compose.yml
└── README.md
```

## Fastest way to verify it — no local Android Studio required

Push the repository to GitHub. The `CI` workflow validates catalog parity, runs backend tests, compiles Android unit tests/lint/instrumentation-test APKs, and builds a debug APK. The resulting APK appears as the **methodra-demo-apk** workflow artifact.

For a GitHub Release, create and push a tag such as:

```bash
git tag v1.0.0-demo
git push origin v1.0.0-demo
```

The release workflow builds a demo APK and backend JAR and attaches them to a GitHub Release. The APK is a **debug-signed portfolio build**, not a Play Store production signing configuration.

## Local commands

```bash
python3 scripts/validate_catalog.py

# Backend
cd backend
./gradlew test
./gradlew bootRun

# Or PostgreSQL + backend together
cd ..
docker compose up --build

# Android, with Android SDK installed
cd android
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

The included `gradlew` bootstrap scripts download the pinned Gradle version when needed (8.13 for Android, 9.5.0 for the backend). They are intentionally small bootstrap scripts rather than a committed Gradle wrapper JAR.

## API examples

The method catalog and matcher do not require an account:

```bash
curl http://localhost:8080/api/v1/methods
```

```bash
curl -X POST http://localhost:8080/api/v1/method-matches \
  -H 'Content-Type: application/json' \
  -d '{
    "goalDomain":"STUDY",
    "desiredOutcome":"Attempt one operating-systems retrieval question after breakfast",
    "obstacle":"DELAYED_START",
    "availableMinutes":35,
    "highScreenTime":true,
    "structureLevel":"MODERATE",
    "pastFailure":"I open social media first"
  }'
```

See `docs/API.md` and `docs/openapi.yaml` for the full surface.

## Evidence and content policy

Evidence labels are data, not marketing decoration:

- **A — Strong support**
- **B — Promising support**
- **C — Practical framework**
- **D — Reflection**

Every research method includes a limitation. Book-inspired collections use original Methodra wording and attribution; the repository does not reproduce modern book passages, cover art, diagrams, or branded exercises. Methodra does not diagnose or claim to treat ADHD, anxiety, depression, addiction, sleep disorders, or other medical conditions.

See `docs/EVIDENCE.md` and `docs/CONTENT_POLICY.md`.

## Privacy boundary

Core use is local-first. Detailed app-usage events are not persisted as a cloud event stream and are not sent to the backend by default. Accessibility protection is optional, reversible and explicitly described in-app. Methodra never claims blocking is impossible to bypass.

See `docs/PRIVACY.md`, `docs/ANDROID_LIMITATIONS.md`, and `docs/THREAT_MODEL.md`.

## Build honesty

The repository contains CI that performs the dependency-resolved Android and Spring builds on GitHub-hosted runners. During preparation of this archive, the catalog validator and dependency-free Kotlin/Java domain code were executed locally, but this packaging environment did not have network access to resolve Android/Spring Gradle dependencies. No APK or green CI result is fabricated in the repository. See `docs/BUILD_STATUS.md`.

## License

Apache-2.0 for Methodra source code and original project content unless a file states otherwise. Third-party names and cited works remain the property of their respective owners.
