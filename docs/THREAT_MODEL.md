# Threat model

## Assets

- local goals/protocol history and personal-trial notes;
- focus-rule package names and schedule configuration;
- optional backend account credentials and bearer tokens;
- synchronized user payloads.

## Main threats and controls

### Credential database compromise
Passwords are stored as BCrypt hashes. Raw bearer tokens are returned to the client once and only SHA-256 token hashes are persisted server-side.

### Stolen bearer token
Tokens expire. Production deployment should add token revocation/logout, TLS-only transport, secret rotation and rate limiting. V1 does not claim those operational controls are complete.

### Silent sync overwrite
Clients send a `baseVersion`; stale versions receive a conflict. The database entity also has JPA optimistic locking. The server does not silently accept a stale overwrite.

### Excessive usage surveillance
Detailed app-usage events remain local by default. Accessibility content retrieval is disabled. The service reacts only to foreground-package transitions for packages selected by the user.

### Focus feature becoming coercive
Protection is opt-in and bypassable. Emergency exit, schedule editing, rule deletion, service disabling and uninstall remain available. There is no financial punishment.

### Malicious catalog configuration
The rule engine supports only typed known fields/operators. JSON cannot execute arbitrary scripts. Catalog validation fails on unknown methods, invalid scores, missing reasons or parity mismatch.

### Sensitive local data on compromised device
Methodra relies on the Android sandbox/device protections in V1 and does not claim protection against a fully compromised/rooted device. Raw detailed usage events are not persisted in Room.

## Out of scope for this demo

Production WAF/rate limits, hosted secrets management, account email verification/recovery, remote data deletion, penetration testing, Play review and a formal privacy-policy/legal review are not represented as completed work.
