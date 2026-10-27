# Saayam For All - Request Service

## Overview
Saayam For All - Request Service is a Java/Spring Boot backend microservice that owns the **lifecycle of help requests** on the Saayam platform. A requester asks for help; this service registers the request, finds and notifies matching volunteers, tracks assignment and status through completion, and keeps requesters and volunteers informed. It runs both as a Spring Boot web application and as AWS Lambda functions behind API Gateway.

Design documents: [Request Microservice Requirements](https://github.com/saayam-for-all/request/blob/main/docs/request-service-requirements.md) and [Request Service Flow Diagram](https://github.com/saayam-for-all/request/blob/main/docs/Saayam-Request-Service-Flow.drawio).

## Features
- **Request Management** - create, view, list (paginated), update, cancel, resume, and delete requests
- **Volunteer Assignment** - assign and remove volunteers on a request, including lead-volunteer handling
- **Help Categories & Metadata** - hierarchical help categories and per-category additional-info fields
- **Volunteer Matching** - in-person (proximity radius) and remote (help-category skill) matching
- **Sentiment-Gated Notifications** - lexicon-based sentiment check before notifying volunteers
- **Multi-Channel Notifications** - email (SES), SMS and push (SNS), and in-app, dispatched asynchronously via SQS
- **Global Exception Handling** - consistent `SaayamResponse` wrapper and error codes
- **Logging with AOP**
- **Internationalization Support**
- **Docker and Kubernetes Deployment Templates**

## Technologies Used
- **Spring Boot 3.3** (Java 17) - Backend framework
- **Maven** (wrapper included) - Build automation
- **PostgreSQL** + Spring Data JPA - Database
- **AWS Lambda, SQS, SNS, SES** - Serverless runtime and messaging
- **springdoc-openapi (Swagger UI)** - API documentation
- **Lombok** - Boilerplate reduction
- **Docker** - Containerization
- **Kubernetes (EKS)** - Deployment

## Project Structure & Explanation
```bash
request/
├── src/
│   ├── main/java/org/sfa/request/
│   │   ├── controller/        # REST controllers handling HTTP requests
│   │   ├── service/           # Business logic (api/ interfaces, impl/ implementations)
│   │   ├── repository/        # Spring Data JPA repositories
│   │   ├── dto/               # Data Transfer Objects for request/response handling
│   │   ├── model/             # JPA entities and enums
│   │   ├── requesthandler/    # AWS Lambda handlers
│   │   ├── exception/         # Custom exceptions and global error handling
│   │   ├── config/            # AWS, OpenAPI, and i18n configuration
│   │   └── utils/             # Utility classes
│   ├── main/resources/lang/   # Localization files for multi-language support
│   └── test/java/             # Unit tests
├── Dockerfile                 # Configuration for containerization using Docker
├── docker-compose.yml         # Local container run
├── deployment.yaml            # Kubernetes deployment configuration
├── Service.yaml               # Kubernetes service configuration
├── ingress.yaml               # Configuration for managing external access via Kubernetes Ingress
├── service-account.yaml       # Kubernetes service account (EKS IAM role)
├── pom.xml                    # Maven dependencies and project build configuration
└── README.md                  # Documentation
```

## Folder & File Descriptions
### `controller/`
Contains controllers that handle HTTP requests and route them to the appropriate services.
- `RequestController.java`: Request CRUD, cancel/resume, and volunteer assignment endpoints.
- `HelpCategoryController.java`: Hierarchical help categories.
- `MetadataController.java`: Additional-info fields per help category.

### `service/`
Contains the business logic.
- `RequestService`: Core request lifecycle logic.
- `VolunteerMatchService` / `VolunteerAssignmentService`: Find and assign volunteers.
- `SentimentAnalysisService`: Screens request text before notifying volunteers.
- `NotificationDispatchService`: Routes notifications to email, SMS/push, and in-app channels.

### `repository/`
Interfaces for database access using Spring Data JPA.
- `RequestRepository.java`: Repository for managing request data.
- `VolunteerMatchRepository.java`: Queries for matching volunteers to requests.

### `dto/`
Data transfer objects used for structured API communication.
- `RequestDTO.java`, `RequestSummaryDTO.java`: Request payloads and list summaries.
- `notification/`: Notification event and recipient objects.

### `exception/`
Handles global exception handling.
- `GlobalExceptionHandler.java`: Manages application-wide exceptions.
- `LambdaExceptionHandler.java`: Handles errors in Lambda invocations.

### `utils/`
Contains utility/helper classes.
- `JsonConverter.java`: JSON conversion helpers.
- `NaturalOrderComparator.java`: Natural-order sorting.

### `resources/lang/`
Localization support for multiple languages (English, German, Spanish, French, Hindi, Japanese, Telugu, Simplified Chinese, Traditional Chinese).
- `saayam_messages.properties`: Default (English) language properties.
- `saayam_messages_hi.properties`: Hindi language support.
- `saayam_messages_zh_CN.properties`: Chinese language support.

### `test/java/`
Includes unit tests for application components.
- `RequestApplicationTests.java`: Ensures the application context loads.

## Installation & Setup
### Prerequisites
- JDK 17 or later
- Maven, or the included `./mvnw` wrapper
- PostgreSQL
- Docker (optional, for containerized deployment)
- Kubernetes (optional, for cloud deployment)

### Clone Repository
```sh
git clone https://github.com/saayam-for-all/request.git
cd request
git checkout dev   # application code lives on dev / test
```

### Build & Run
#### Run Locally
Start PostgreSQL and set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`, then:
```sh
./mvnw clean install
./mvnw spring-boot:run
```

#### Run with Docker
The Dockerfile copies a pre-built jar, so build it first:
```sh
./mvnw clean package -DskipTests
docker build -t saayam-request .
docker run -p 8080:8080 saayam-request
```

## API Documentation
Once the application is running, Swagger API documentation is available at:
```sh
http://localhost:8080/swagger-ui/index.html
```

## Deployment
Replace the `<placeholder>` values in the manifests, then deploy using Kubernetes:
```sh
kubectl apply -f service-account.yaml
kubectl apply -f deployment.yaml
kubectl apply -f Service.yaml
kubectl apply -f ingress.yaml
```

## Contributing
1. **Create** a new branch from the target branch (`dev`, `test`, or `main`)
2. **Commit** your changes
3. **Push** your branch and submit a **pull request** to the matching branch
4. Open a **separate pull request for each target branch**; changes are not merged between branches automatically

## License
This project is licensed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for details.

## Contact
For any queries or contributions, reach out to:
- Saayam for All volunteers
- **GitHub Repository**: [Saayam For All - Request](https://github.com/saayam-for-all/request)
- **Spring Boot**: [Spring Boot](https://spring.io/projects/spring-boot)
