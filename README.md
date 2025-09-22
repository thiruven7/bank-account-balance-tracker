# Bank Account Balance Tracker
Application to process credit and debit transactions, track account balances, and publish batched transaction data to downstream audit system with a user interface to display the real-time account balances.

---

## Architecture Overview

The solution is split into three components, each running in its own JVM or process:

1. **Transaction Producer** – generates random credit and debit transactions and sends them to the Balance Tracker API.  
2. **Balance Tracker API** – processes incoming transactions, updates the balance, and batches transactions for submission to the audit system.  
3. **Balance Tracker UI** – a React-based user interface that displays the account balance and periodically refreshes it.  

These components interact via REST APIs.
Transactions flow from the Producer → Balance Tracker API → Audit submission process, while the UI queries the Balance Tracker API to display balances.


---

## 1. Transaction Producer

### Tech Stack
- Java 17  
- Spring Boot  
- Maven
- Springdoc OpenAPI (Swagger)
- Spring Actuator (health, metrics)
- RestTemplate for REST communication
- SLF4J with file-based logging for audit traceability  

### Prerequisites
- Java 17  
- Maven 3.9+  

### How it works
The producer generates transactions on two dedicated threads: one for credits and one for debits. Each transaction has a unique transaction ID and an amount between £200 and £500,000. Debits are represented with negative amounts, credits with positive amounts.  
The producer sends 25 credits and 25 debits per second (50 total) to the Balance Tracker API.

### Key Endpoints
- `POST /api/producer/v1/start` → Starts the producer threads.
- `POST /api/producer/v1/stop` → Stops the producer threads.
- `GET /api/producer/v1/status` → Returns `RUNNING` or `STOPPED`.

### API Documentation
- **Swagger UI:** [http://localhost:8092/swagger-ui.html](http://localhost:8092/swagger-ui.html)  
- **OpenAPI spec JSON:** [http://localhost:8092/v3/api-docs](http://localhost:8092/v3/api-docs) 
- **OpenAPI spec document:** - `/docs/transaction-producer.yml`

### Actuator Endpoints
- Running on port **9092**  
- Health URL: [http://localhost:9092/actuator/health](http://localhost:9092/actuator/health)   

### Configuration
```yaml
server:
  port: 8092
  
spring:
  application:
    name: transaction-producer

msa:
  balance-tracker-api:
    url: http://localhost:8091
    path: /api/bankaccount/v1/transactions
  producer:
    threadpoolcount: 2
    client:
      retry-count: 3 # No of time to retry
      retry-delay: 100 # in milliseconds
    rate:
      credits-per-sec: 25 # Number of credit transactions
      debits-per-sec: 25 # Number of debit transactions
    amount:
      min: 200 # Min amount per transaction in £
      max: 500000 # Max amount per transaction in £

# Actuator Properties
management:
  server:
    port: 9092
  endpoints:
    web:
      exposure:
        include: health, info, env, metrics, prometheus
  endpoint:
    health:
      show-details: always

#Logger properties
logging:
  level:
    root: INFO # Set to INFO for PROD env.
    com.bankaccount.balancetracker: DEBUG # Set to INFO for PROD env.
```
- **Base URL:** Configurable in `application.yml` (`msa.balance-tracker-api.url` and `msa.balance-tracker-api.path`) 
- **Transaction rate:** 25 credits/sec + 25 debits/sec  
- **Amount range:** £200 to £500,000  
- **Retry logic:** 3 attempts with 100ms delay  
- - **Logging:** DEBUG for local traceability; INFO recommended for production.  
- **Actuator:** exposed on port **9092** for health and metrics 

### Current Implementation

- Runs as a separate JVM from the Balance Tracker API  
- Spring Boot microservice
- Uses two dedicated threads to continuously generate transactions:  
  - One thread for credits (positive amounts)  
  - One thread for debits (negative amounts)  
- Each transaction includes:  
  - A randomly generated ID (prefixed with CRE or DEB)  
  - A random amount within the configured range  
- Transactions are sent via REST to the Balance Tracker API  
- Retry logic handles transient failures with configurable delay  
- REST endpoints allow lifecycle control (`/start`, `/stop`, `/status`) 
- JUnit with Mockito extension test cases 
- Swagger UI and OpenAPI spec available for all exposed endpoints  
- Actuator endpoints provide health, metrics, and Prometheus integration  

### Production Enhancements

- Replace retry logic with **Resilience4j** (exponential backoff, circuit breaker)  
- Redirect failed transactions to **Kafka/RabbitMQ** or a **DB** for reprocessing  
- Add **Prometheus metrics** for transaction rate, retry count, and batch size    
- Include **OAuth2/JWT authentication and authorization** for REST APIs to control access

### How to Run
1. **Clone the repository**:
```bash
git clone https://github.com/thiruven7/bank-account-balance-tracker.git
cd bank-account-balance-tracker/transaction-producer
```
2. **Build the project**:
```bash
mvn clean install
```
3. **Run the application**:
```bash
mvn spring-boot:run
```

---

## 2. Balance Tracker API

### Tech Stack
- Java 17 
- Spring Boot  
- Maven 
- Springdoc OpenAPI for Swagger documentation 
- Spring Actuator for health and metrics
- H2 Database (for integration testing and local persistence)
- SLF4J with file-based logging for audit traceability

### Prerequisites
- Java 17  
- Maven 3.9+  

### How it works
The Balance Tracker API exposes endpoints to add transactions and retrieve the balance. It maintains the current balance in memory and holds transactions in an in-memory queue. 
Once 1000 transactions have been accumulated, they are batched and submitted to the audit system. Batches are created such that no batch exceeds a total absolute value of £1,000,000, and the number of batches is minimized.

### Key Endpoints
- `POST /api/bankaccount/v1/transactions` → Submit a transaction (credit or debit).  
- `GET /api/bankaccount/v1/balance` → Retrieve the current account balance.  

### API Documentation
- **Swagger UI:** [http://localhost:8091/swagger-ui.html](http://localhost:8091/swagger-ui.html)  
- **OpenAPI spec JSON:** [http://localhost:8091/v3/api-docs](http://localhost:8091/v3/api-docs) 
- **OpenAPI spec document:** - `/docs/balance-tracker-api.yaml`

### Actuator Endpoints
- Running on port **9091**  
- Health URL: [http://localhost:9091/actuator/health](http://localhost:9091/actuator/health)  

### Configuration
```yaml
server:
  port: 8091

msa:
  auditsystem:
    transaction:
      limit: 1000 # Max Transaction Limit to submit to the Audit System.
      maxAmountPerBatch: 1000000 # Max absolute amount per batch in £.
    scheduler:
      delay-ms: 30000 # in millisecnds
  bank:
    transaction:
      amount:
        min: 200 # Min amount per transaction in £
        max: 500000 # Min amount per transaction in £
spring:
  application:
  name: balance-tracker-api
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: delta
    password: springwater
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  h2:
    console:
      enabled: true
      path: /h2-console
  
# Actuator Properties
management:
  server:
    port: 9091
  endpoints:
    web:
      exposure:
        include: health, info, env, metrics, prometheus
  endpoint:
    health:
      show-details: always

#Logger properties
logging:
  level:
    root: INFO # Set to INFO for PROD env.
    com.bankaccount.balancetracker: DEBUG # Set to INFO for PROD env.

```
- **Audit trigger:** Fires every 30 seconds or when 1000 transactions are accumulated.  
- **Batching logic:** Ensures no batch exceeds £1,000,000 in absolute value.  
- **Transaction validation:** Amount range enforced between £200 and £500,000.  
- **Persistence:** Uses H2 in-memory DB for local testing; replace with PostgreSQL for production.  
- **Actuator:** Exposed on port **9091** for health, metrics, and Prometheus integration.  
- **H2 Console:** Available at `/h2-console` for inspection during development.  
- **Logging:** DEBUG for local traceability; INFO recommended for production.

### Current Implementation
- The API exposes endpoints to submit transactions and retrieve the current account balance.  
- Transactions are persisted to an H2 database using Spring Data JPA in a dedicated `TransactionT` entity.  
- The current balance is derived from persisted transactions and stored in a dedicated `BalanceT` entity.  
- Applied **pessimistic locking** to prevent lost updates under concurrent transaction load.
- Once 1000 transactions are accumulated, they are batched and submitted to the audit system.  
- Batches are created such that:
  - No batch exceeds a total absolute value of £1,000,000  
  - The number of batches is minimized using a FFD bin-packing strategy  
- Audit submissions are logged to timestamped JSON files under `resources/audit-logs` for traceability.
- Integration tests use H2 to validate persistence, batching, and audit logic.
- JUnit with Mockito extension test cases
- Swagger UI and OpenAPI spec available for all exposed endpoints  
- Actuator endpoints provide health, metrics, and Prometheus integration

## Prototype Implementation
- In-memory storage using `ConcurrentLinkedQueue` and `AtomicReference`  
- Suitable for prototyping or stateless deployments  
- Not recommended for production due to lack of durability

### Production Enhancements
- **Replace in-memory H2 DB** with PostgreSQL or another persistent database for durability.  
- **Pessimistic locking:** `SELECT ... FOR UPDATE SET LOCK` to prevent lost updates under concurrent load.  
- **Decouple batching logic:** use Spring Boot Batch or a scheduler to flush incomplete batches.  
- **Decouple transaction processing:** use Kafka or RabbitMQ for real-time downstream consumption.  
- **Separate audit tracking table** for balance update history.  
- **Security:** add JWT/OAuth2 for API endpoints.  
- **Horizontal scaling:** run multiple API instances with shared DB or message queue coordination.  
- **Observability:** integrate Prometheus and OpenTelemetry for metrics and tracing.

### How to Run
1. **Clone the repository**:
```bash
git clone https://github.com/thiruven7/bank-account-balance-tracker.git
cd bank-account-balance-tracker/balance-tracker-api
```
2. **Build the project**:
```bash
mvn clean install
```
3. **Run the application**:
```bash
mvn spring-boot:run
```
---

## 3. Balance Tracker UI

### Tech Stack
- ReactJS  
- Axios for API calls  
- Bootstrap (or chosen CSS framework) for styling  

### Prerequisites
- Node.js 18+  
- npm or yarn  

### How it works
The UI displays a **static Account ID label** and the **current balance**, which is dynamically retrieved from the Balance Tracker API. The balance is refreshed every 3 seconds to provide near real-time updates.  
The UI runs as a standalone React application and calls the `GET /api/bankaccount/v1/balance` endpoint to fetch the latest balance.

### Running the UI
```bash
git clone https://github.com/thiruven7/bank-account-balance-tracker.git
cd balance-tracker-ui
npm install
npm start
```
Default dev server runs at [http://localhost:3000](http://localhost:3000).  

### Current Implementation
- Simple card-based UI with periodic balance refresh.  
- No authentication.  

### Production Enhancements
- Implement robust error handling and retries for failed API calls.  
- Secure API endpoints using JWT or OAuth2.  
- Extend the UI to include transaction history and audit submission views.  
- Containerize the application using Docker for consistent deployment across environments.  