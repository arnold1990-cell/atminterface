# ATM Interface Application

Full-stack ATM app with a Spring Boot backend and a vanilla JavaScript frontend.

## Tech Stack
- Backend: Spring Boot 3.x, Java 21, REST API
- DB: PostgreSQL (default local dev/runtime)
- Migrations: Flyway
- Frontend: HTML, CSS, Vanilla JS (SPA hide/show screens)
- Auth: card number + PIN login with session token (`X-Session-Token`)

## Project Structure (package-by-feature)
- `auth`: login/logout flow
- `accounts`: customer/account entities and repositories
- `transactions`: transaction entity/repository/DTO
- `atm`: ATM operations and endpoints
- `common`: API response wrapper, config, security/session, global exception handling
- `frontend/`: ATM UI (`index.html`, `styles.css`, `app.js`)

## Database (PostgreSQL)
Default connection settings (overridable with env vars):
- `DB_URL=jdbc:postgresql://localhost:5432/atmdb`
- `DB_USERNAME=postgres`
- `DB_PASSWORD=postgres`

Example local setup with Docker:
```bash
docker run --name atm-postgres -e POSTGRES_DB=atmdb -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16
```

## Run Backend
```bash
./mvnw spring-boot:run
```

Backend base URL: `http://localhost:8080`

## Run Frontend
- Open `frontend/index.html` directly in a browser, or
- Use a static server / VSCode Live Server.

Frontend expects backend at `http://localhost:8080`.

## Test Credentials
- Card Number: `1234567890123456`
- PIN: `1234`

## API Response Format
All endpoints return:
```json
{ "success": true, "message": "...", "data": {} }
```

## Endpoints
### Auth
- `POST /api/auth/login`
- `POST /api/auth/logout`

### ATM (protected)
- `GET /api/atm/balance`
- `POST /api/atm/withdraw`
- `POST /api/atm/deposit`
- `POST /api/atm/transfer`
- `GET /api/atm/statement?limit=10`
- `POST /api/atm/change-pin`

## Notes
- PIN values are stored hashed with BCrypt.
- A startup initializer ensures the sample user's PIN is set to `1234` (BCrypt-encoded) and account balance is seeded.
