# Frontend ↔ Backend Connectivity Verification

## What was checked

1. **Frontend API base URL resolution** (`frontend/app.js`)
   - The UI computes API base candidates and includes `http://localhost:8080/api` when served from a non-8080 frontend dev host.
   - API calls are made to `${base}${path}` where `path` values include `/auth/login`, `/auth/logout`, and `/atm/*`.

2. **Backend route prefixes**
   - `AuthController` is mounted at `/api/auth`.
   - `AtmController` is mounted at `/api/atm`.

3. **Cross-origin compatibility**
   - Global CORS mapping allows all headers/methods, which supports frontend dev-server calls to backend.

## Conclusion

From code inspection, the frontend is configured to connect to the backend correctly (same `/api` base and matching endpoint paths).

## Runtime verification status in this environment

A full runtime check could not be executed here because dependency downloads are blocked by registry/network policy (`403 Forbidden` from npm and Maven Central), so backend/frontend processes could not be started in this container.
