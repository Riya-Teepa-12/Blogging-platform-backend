# Blogging Platform

A comprehensive blogging platform backend architecture featuring microservices, API Gateway, Service Registry, and various domain-specific services like Auth, Post, Comment, Category, Media, Notification, and Newsletter services.

## Architecture
The platform is built using a microservices architecture. Key components include:
- **API Gateway**: Entry point for all client requests.
- **Service Registry**: Eureka server for service discovery.
- **Auth Service**: Handles user authentication and authorization.
- **Post Service**: Manages blog posts.
- **Comment Service**: Manages comments on posts.
- **Category Service**: Manages post categories.
- **Media Service**: Handles media uploads and retrieval.
- **Notification Service**: Manages user notifications (via Kafka).
- **Newsletter Service**: Manages newsletter subscriptions.

## Tech Stack
- Java
- Spring Boot
- Spring Cloud (Gateway, Eureka)
- MySQL
- Apache Kafka
- Docker & Docker Compose

## Setup and Running
To run the project locally, refer to the `RUN_CONFIGURATION.md` file for detailed instructions.

### Starting the Backend
You can use the provided PowerShell scripts to start and stop the backend services:
```powershell
./start-backend.ps1
```

### Stopping the Backend
```powershell
./stop-backend.ps1
```

## License
This project is licensed under the MIT License.
