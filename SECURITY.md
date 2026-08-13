# Security policy

Please report security issues privately through GitHub's private vulnerability reporting feature when available. Do not publish tokens, credentials, private user exports, or exploit details in a public issue.

Methodra's backend uses opaque bearer tokens stored only as SHA-256 hashes server-side and BCrypt password hashes. The Android client does not require an account for core use and does not upload detailed application-usage events by default.

See `docs/THREAT_MODEL.md` for the current boundaries and known limitations.
