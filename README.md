# Inkwell - Microservices Blogging Platform

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen.svg)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-Gateway%20%7C%20Eureka-blue.svg)

Inkwell is a comprehensive, modular blogging platform backend built using a modern microservices architecture. It supports content creation, community interactions, media storage, and subscription-driven features with clear security boundaries.

## 🚀 Key Features

- **Authentication & Authorization:** JWT-based auth with role-based access control (`READER`, `AUTHOR`, `ADMIN`).
- **Post Management:** Complete lifecycle management including drafts, publishing, featuring, view counts, and likes.
- **Community Engagement:** Commenting system with moderation, replies, and mentions.
- **Taxonomy:** Robust category and tag management, including trending tags.
- **Media Handling:** Media uploads, metadata updates, and optional S3 storage support.
- **Real-time Notifications:** Event-driven notifications via Apache Kafka (in-app and email).
- **Newsletters:** Subscription management, preferences, and post broadcast emails.
- **Admin Capabilities:** Audit logs, user management, and author upgrade workflows.

## 🏗 System Architecture

The platform is organized into independent Spring Boot services registered in a Eureka Service Registry. Traffic enters via the API Gateway (Spring Cloud Gateway), which handles routing and JWT validation.

### Microservices
- `api-gateway` (Port 8080): Entry point, routing, and JWT validation.
- `service-registry` (Port 8761): Eureka server for service discovery.
- `Auth-service` (Port 8081): Identity, roles, and admin audit logging.
- `post-service` (Port 8082): Post CRUD, views, and likes.
- `comment-service` (Port 8083): Comment threads and moderation.
- `category-service` (Port 8084): Categories and tags.
- `media-service` (Port 8085): Media uploads and storage abstraction.
- `newsletter-service` (Port 8086): Subscriptions and broadcast emails.
- `notification-service` (Port 8087): Notification dispatch (via Kafka) and caching.

## 🛠 Tech Stack

- **Core:** Java 17, Spring Boot 3.2.x, Spring Web, Spring Security, Spring Data JPA
- **Cloud & Routing:** Spring Cloud Gateway, Netflix Eureka
- **Messaging & Events:** Apache Kafka, Spring Kafka
- **Database:** MySQL (Shared logical schema), H2 (Local/Dev)
- **Caching:** Redis
- **Documentation:** Springdoc OpenAPI
- **Build & DevOps:** Maven multi-module, Docker Compose (Kafka/ZooKeeper)

## 🚦 Getting Started

### Prerequisites
- Java 17
- Maven
- (Optional) Docker for Kafka/Redis/MySQL

### Running Locally

All services run by default using an in-memory **H2 database**. No DB credentials are required for local development.

**Backend Startup Order:**
1. Start `service-registry`
2. Start `api-gateway`
3. Start all other domain services (`Auth-service`, `post-service`, etc.)

*Alternatively, use the provided PowerShell script:*
```powershell
./start-backend.ps1
```

For more detailed setup instructions, including CORS setup, database configuration for MySQL/PostgreSQL, and frontend integration, please see the [RUN_CONFIGURATION.md](./RUN_CONFIGURATION.md) and [SDD.md](./SDD.md) documents.

## 🗄️ Database Configuration (Production)
To run services with MySQL or PostgreSQL, configure the environment variables per service. Example for Auth Service:
`AUTH_DB_URL`, `AUTH_DB_USERNAME`, `AUTH_DB_PASSWORD`, `AUTH_DB_DRIVER`, `AUTH_JPA_DIALECT`

## 📄 License
This project is licensed under the MIT License.
