# Android focus-protection limitations

Methodra's focus feature is intentionally described as **best effort**, not tamper-proof blocking.

- Usage Access is a special user-granted setting. Methodra cannot silently grant it.
- AccessibilityService is optional and can be disabled by the user or OS.
- The service observes foreground package transitions, not screen text/content.
- Android/OEM behavior can limit background activity launches or change latency.
- A user can uninstall Methodra, disable permissions/services, or otherwise bypass protection.
- Daily usage totals come from Android's usage statistics and may not equal a perfect real-time accounting system.
- Recurring schedules use the device's current local wall clock. Timezone changes therefore move the schedule with the user, which is deliberate for V1.
- Overnight schedule evaluation is tested as pure date/time boundary logic, but device-level DST and OEM behavior still need hardware coverage.
- Play distribution requires a current Accessibility policy review and accurate disclosure of the feature's core purpose.

The safety path is a product requirement: focus sessions have a reasoned emergency exit with a short deliberate cool-down, recurring schedules can be disabled/deleted, budget rules can be removed, and the OS service can always be turned off.
