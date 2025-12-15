# 📚 Project Readme: Prices Service (WebFlux, Hexagonal Architecture)

This document provides a comprehensive overview of the **Prices Service**, an application designed to retrieve the most applicable product pricing based on date, product, and brand criteria.

This service is built using the latest **Java 21** features, leveraging the **Spring WebFlux** reactive stack and strictly adhering to the **Hexagonal Architecture (Ports and Adapters)** pattern to ensure maintainability, testability, and technology decoupling.

---

## 🚀 Core Technology Stack

| Category | Technology | Explanation |
| :--- | :--- | :--- |
| **Language** | Java 21 | Utilizes modern Java features for clean and efficient code. |
| **Web Stack** | Spring WebFlux / Reactor | Non-blocking, reactive framework optimized for high concurrency and throughput. |
| **Architecture** | Hexagonal (Ports & Adapters) | Isolates core business logic from external dependencies (databases, frameworks). 

[Image of Hexagonal Architecture Diagram]
|
| **Persistence** | R2DBC | Reactive, non-blocking API for relational database access, essential for WebFlux. |
| **In-Memory DB** | H2 Database | In-memory database used for development and testing. |
| **Resilience** | Resilience4j | Implements the **Circuit Breaker** pattern to manage dependency failures (e.g., R2DBC). |
| **Documentation** | Springdoc-OpenAPI | Automatic generation of interactive documentation (Swagger UI). |

---

## 📐 Architecture: Hexagonal Pattern

The project structure is organized following the **Hexagonal Architecture**. This ensures the core business logic is independent of external technologies (API or DB).

* **`Domain`**: Contains pure business rules and entities (technology-agnostic).
* **`Application`**: Defines the use cases (Ports) and orchestrates the logic.
* **`Infrastructure` (Adapters):** Connects the application to the outside world.
    * **Driving Adapter (`in/api`):** The API layer (WebFlux Controller).
    * **Driven Adapter (`out/repository`):** The persistence layer (R2DBC implementation).

---

## ⚙️ Configuration and Resilience Details

The application uses the `application.yaml` configuration to define reactive behavior, R2DBC connectivity, and the resilience policy.

### 1. Spring WebFlux and R2DBC

| Configuration Key | Value | Explanation |
| :--- | :--- | :--- |
| `spring.r2dbc.url` | `r2dbc:h2:mem:///pricesdb` | Configures the H2 database in reactive, in-memory mode. |
| `spring.h2.console.enabled` | `true` | Enables the H2 web console for inspecting the DB (accessible at `/h2-console`). |
| `spring.sql.init.mode` | `always` | Ensures that schema (`schema.sql`) and data (`data.sql`) scripts are executed upon every startup. |

### 2. Circuit Breaker Configuration (`Resilience4j`)

The Circuit Breaker (`priceR2dbcCircuitBreaker`) is applied to the persistence adapter (R2DBC) to prevent cascading failures if the database becomes slow or unavailable.

| Parameter | Value | Policy Detail |
| :--- | :--- | :--- |
| `failureRateThreshold` | `50` | The circuit opens if the failure rate reaches **50%**. |
| `minimumNumberOfCalls`| `5` | At least **5 calls** are required for the Circuit Breaker to start calculating the failure rate. |
| `waitDurationInOpenState` | `5s` | The circuit remains open for **5 seconds**. During this time, calls are immediately rejected with a `ServiceUnavailableException` (503). |
| `slidingWindowSize` | `10` | The failure rate is calculated over the last **10 calls**. |
| `slidingWindowType` | `COUNT_BASED` | The evaluation window is based on the number of calls, not time. |

**Functioning:** If R2DBC calls fail in 5 out of the last 10 attempts, the circuit opens. Subsequent requests will bypass the persistence layer for 5 seconds, protecting the service.

---

## 💻 Running and Access

1.  **Requirements:** Java 21+ and Maven/Gradle.
2.  **Execution:** Start the application using the Spring Boot runner:
    ```bash
    $ mvn spring-boot:run
    ```
3.  **Access URLs:**
    * **API (Swagger UI):** `http://localhost:8080/swagger-ui`
    * **H2 Console:** `http://localhost:8080/h2-console` (Use the R2DBC URL from the YAML file).

### Example API Query

The main endpoint is designed to find the applicable tariff with the highest priority (`ORDER BY priority DESC`).

**Endpoint:** `GET /api/v1/prices`

| Parameter | Description                                              |
| :--- |:---------------------------------------------------------|
| `brandId` | Identifier for the brand/chain (e.g., `1`).              |
| `productId` | Identifier for the product (e.g., `35455`).              |
| `applicationDate` | Application date and time (e.g., `14/06/2020 16:00:00`). |

**Example Request (CLI):**

```bash
curl -X GET "http://localhost:8080/api/v1/prices?brandId=1&productId=35455&applicationDate=2020-06-14T16:00:00"