# E-Commerce Microservices Platform

A distributed e-commerce platform built using **Java and Spring Boot microservices**, designed to demonstrate a scalable, resilient, and observable microservices architecture.

The platform is divided into independent services responsible for product management, order processing, inventory management, and notifications. An **API Gateway** provides a centralized entry point for client requests, while **Eureka Service Registry** enables service discovery between microservices.

The system uses **Apache Kafka** for asynchronous, event-driven communication and REST-based synchronous communication for operations that require an immediate response. **Resilience4j** is used to improve fault tolerance between services.

The application is containerized using **Docker** and deployed using **Kubernetes**. End-to-end observability is implemented using **OpenTelemetry, Prometheus, Grafana, Loki, and Tempo** for collecting metrics, logs, and distributed traces.

## Architecture

The platform follows a microservices architecture consisting of the following components:

- **API Gateway** – Central entry point for client requests and routing to backend services.
- **Product Service** – Handles product-related operations and product data.
- **Order Service** – Manages order creation and order-related operations.
- **Inventory Service** – Manages product inventory and stock-related operations.
- **Notification Service** – Handles notification-related operations and asynchronous events.
- **Eureka Service Registry** – Provides service discovery for the microservices.
- **Authentication Service** – Handles authentication and secure access to the application.
- **Apache Kafka** – Enables asynchronous event-driven communication between services.
- **MongoDB / MySQL** – Provides persistent storage for application data.
- **Kubernetes** – Manages containerized services and service-to-service communication.
- **Observability Stack** – Provides application metrics, logs, and distributed tracing.

## Tech Stack

- Java
- Spring Boot
- Spring Web / REST APIs
- Spring Data JPA
- MySQL
- Spring Cloud
- Spring Cloud Gateway
- Eureka Service Discovery
- Resilience4j
- Apache Kafka
- Event-driven asynchronous communication
- Synchronous REST communication
- Docker
- Kubernetes
- Kubernetes Services
- Keycloak
