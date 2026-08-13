# Milestone 0 — platform risk prototypes

## A. Interactive stone

Implemented as an original procedural Compose Canvas object rather than a copied asset.

Acceptance checks represented in code:

- eight discrete fracture stages;
- growing internal amber reveal and small dust marks;
- optional haptic feedback;
- skip path from the first screen;
- reduced-motion path removes entrance/exit fades;
- final geometric core is original to Methodra.

**Still requires real-device verification:** frame pacing and haptic feel across low/mid/high Android hardware. The repository does not claim 60 FPS has been measured where it has not.

## B. Usage access and focus protection

Implemented:

- Usage Access permission education and settings link;
- local `UsageStatsManager` totals;
- selected-app focus rules and daily budgets;
- focus timer with deliberate emergency exit reason and short cool-down;
- recurring local-time schedules, including overnight windows;
- optional AccessibilityService that observes foreground package changes only;
- window-content retrieval disabled;
- permanent OS-level ability to disable the service;
- best-effort redirect activity when a selected app is opened.

**Still requires real-device verification:** OEM-specific background behavior, activity-launch restrictions, permission UX, latency, Doze/reboot behavior, and Play policy review before distribution. No statement in the code or UI claims the restriction cannot be bypassed.
