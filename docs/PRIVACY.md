# Privacy model

## Default: local first

Methodra's daily protocol, focus configuration, usage summaries, trial logs and notes are useful without an account. Core behavior should continue when the backend is absent.

## Application usage

`UsageStatsManager` is queried only after the user grants Usage Access. Methodra uses it to calculate local foreground-time totals for the current day and to evaluate a selected daily budget. The app does not upload a stream of detailed package-usage events to the backend by default.

Selected package names in focus rules are local application data. The optional sync backend is generic and is not wired to automatically receive raw usage events.

## AccessibilityService

The optional focus service listens only for foreground window-state changes and is configured with `canRetrieveWindowContent=false`. Its purpose is to notice that a user-selected distracting package became foreground while a protection rule is active. It must remain obvious in Android settings and easy to disable.

## Export and deletion

Settings can export local Methodra state as JSON and can clear local protocol/focus/trial tables. If server sync is later enabled in the Android client, server-side account/data deletion must be added before calling that integration production-ready.

## Encryption

V1 relies on Android's application sandbox/device storage protection and does not add a separate SQLCipher layer. Raw detailed usage events are not persisted in Room. If future versions store materially more sensitive journal or health-adjacent content, the storage threat model should be revisited before release.
