# Inkwell Run Configuration

## 1) Frontend

Create `inkwell-frontend/.env` from `inkwell-frontend/.env.example`:

`VITE_API_BASE_URL=http://localhost:8080`

Run:

`cd inkwell-frontend`

`npm install`

`npm run dev`

## 2) Backend startup order

1. `service-registry` on `8761`
2. `api-gateway` on `8080`
3. `Auth-service` on `8081`
4. `post-service` on `8082`
5. `comment-service` on `8083`
6. `category-service` on `8084`
7. `media-service` on `8085`
8. `newsletter-service` on `8086`
9. `notification-service` on `8087`

You can also use `start-backend.ps1` from project root.

## 3) Database configuration

All services run by default on H2 file database. No DB credentials are required for local run.

If you want MySQL/PostgreSQL, set env vars per service:

- Auth service: `AUTH_DB_URL`, `AUTH_DB_USERNAME`, `AUTH_DB_PASSWORD`, `AUTH_DB_DRIVER`, `AUTH_JPA_DIALECT`
- Post service: `POST_DB_URL`, `POST_DB_USERNAME`, `POST_DB_PASSWORD`, `POST_DB_DRIVER`, `POST_JPA_DIALECT`
- Comment service: `COMMENT_DB_URL`, `COMMENT_DB_USERNAME`, `COMMENT_DB_PASSWORD`, `COMMENT_DB_DRIVER`, `COMMENT_JPA_DIALECT`
- Category service: `CATEGORY_DB_URL`, `CATEGORY_DB_USERNAME`, `CATEGORY_DB_PASSWORD`, `CATEGORY_DB_DRIVER`, `CATEGORY_JPA_DIALECT`
- Media service: `MEDIA_DB_URL`, `MEDIA_DB_USERNAME`, `MEDIA_DB_PASSWORD`, `MEDIA_DB_DRIVER`, `MEDIA_JPA_DIALECT`
- Newsletter service: `NEWSLETTER_DB_URL`, `NEWSLETTER_DB_USERNAME`, `NEWSLETTER_DB_PASSWORD`, `NEWSLETTER_DB_DRIVER`, `NEWSLETTER_JPA_DIALECT`
- Notification service: `NOTIFICATION_DB_URL`, `NOTIFICATION_DB_USERNAME`, `NOTIFICATION_DB_PASSWORD`, `NOTIFICATION_DB_DRIVER`, `NOTIFICATION_JPA_DIALECT`

## 4) CORS

Gateway now allows frontend origin:

`GATEWAY_ALLOWED_ORIGIN=http://localhost:5173`

If frontend runs on another host/port, change this env var before starting `api-gateway`.

## 5) Google/GitHub credentials

No Google/GitHub client ID or secret is required in current backend implementation.

Current auth supports:

- Local login/register with email + password
- Programmatic `/auth/oauth/login` payload login

If you want full OAuth redirect flow (Google/GitHub button -> provider -> callback), that is a separate implementation and then client ID/client secret will be required.

## 6) Admin and Author access

Frontend now protects:

- `/author`: only `AUTHOR` or `ADMIN`
- `/admin`: only `ADMIN`

Regular users cannot open these pages directly anymore.
