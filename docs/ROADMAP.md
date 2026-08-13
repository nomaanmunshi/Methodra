# Roadmap

## Version 1 — current repository scope

- Android app only
- deterministic 10-method engine
- exactly four book-inspired collections
- one active protocol
- Today / Methods / Focus / Lab
- local-first Room/DataStore state
- selected-app focus protection, budgets and schedules
- one-primary-metric personal trial
- weekly/adaptation decisions
- optional backend account/sync primitives
- export/delete and evidence/source visibility

## Before calling V1 production-ready

- get CI green on the target GitHub repository;
- test the focus service on multiple real OEM devices and Android versions;
- measure onboarding frame pacing and tune haptics;
- add first Room migration when schema version 2 exists, then enforce migration tests;
- add backend token revocation/logout, account deletion, password recovery and deployment rate limiting if cloud accounts ship;
- perform accessibility audit with TalkBack/font scaling/reduced motion;
- complete current Play Accessibility policy review and privacy disclosures;
- produce signed production APK/AAB with protected signing credentials.

## Post-V1 candidates, only after the above is stable

- Atomic Habits-inspired environment design
- Tiny Habits-inspired minimum action
- GTD-inspired clear loop
- Digital Minimalism-inspired technology values audit
- richer baseline/secondary metrics in Personal Lab
- opt-in encrypted cloud sync

Not planned for V1: AI coach, social network, payments, public leaderboards, iOS, wearables, calorie tracking or hundreds of templates.
