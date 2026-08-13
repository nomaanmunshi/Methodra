# Backend API

Base path: `/api/v1`

## Public catalog

### `GET /methods`
Returns the ten research-supported Method definitions including evidence assessment, limitations, steps and source references.

### `GET /book-collections`
Returns the four book-inspired V1 collections.

### `POST /method-matches`
Runs the deterministic server matcher and returns at most three ranked results.

Example body:

```json
{
  "goalDomain": "STUDY",
  "desiredOutcome": "Attempt one retrieval question after breakfast",
  "obstacle": "DELAYED_START",
  "availableMinutes": 35,
  "highScreenTime": true,
  "structureLevel": "MODERATE",
  "pastFailure": "I open social media first"
}
```

## Authentication

### `POST /auth/register`

```json
{"email":"person@example.com","password":"a-long-password"}
```

### `POST /auth/login`
Same request shape. Successful responses include an opaque bearer token and expiry timestamp.

The demo backend does not implement email verification, password reset or token revocation yet; those are production hardening tasks, not hidden assumptions.

## Optional sync

Authenticated with `Authorization: Bearer <token>`.

### `GET /sync/state`
Returns `{version, payload, updatedAt}`. New accounts start at version `0` and payload `{}`.

### `PUT /sync/state`

```json
{
  "baseVersion": 0,
  "payload": "{\"protocols\":[]}"
}
```

A stale `baseVersion` returns HTTP `409` with the current server version. Payload is intentionally opaque JSON text at this layer so the Android app can evolve local schemas without the backend receiving raw usage events by default.

## Health

`GET /actuator/health`

The machine-readable contract is `docs/openapi.yaml`.
