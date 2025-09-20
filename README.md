# Bank Account Balance Tracker
Application to process credit and debit transactions, track account balances, and publish batched transaction data to downstream audit system with a user interface to display the real-time account balances.

---

## Architecture Overview

The solution is split into three components, each running in its own JVM or process:

1. **Transaction Producer** – generates random debit and credit transactions and sends them to the Balance Tracker API.  
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
- RestTemplate for REST communication  

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

### Configuration
- **Base URL** of the Balance Tracker API is configurable in `application.yml` (`msa.balance-tracker-api.url` and `path`).  
- **Retry mechanism** is built in for reliability. Transactions are retried on transient errors.  

### Current Implementation
- Transactions are generated continuously in memory using two dedicated threads — one for credits and one for debits.
- Transactions are sent directly to the Balance Tracker API via REST.
- Retry logic is supported with configurable attempts and delay to handle transient failures.
- The producer exposes REST APIs to control its lifecycle.

### Production Enhancements
- Replace simple retry logic with exponential backoff or circuit breaker (Resilience4j).  
- Failed transactions could be redirected to a dead-letter queue (Kafka/RabbitMQ) or DB for later reprocessing.  
- Metrics and tracing could be added for observability.  

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

### Current Implementation
- Balance and transactions are stored in memory using `ConcurrentLinkedQueue` and `AtomicReference`.  
- Submissions to the audit system are simulated by logging the batch details to the console.  

### Production Enhancements
- Replace in-memory storage with a persistent database (i.e PostgreSQL) for durability.
- Consider using Kafka or message queues to decouple transaction processing from audit submission or any downstream systems. 
- Implement a scheduler or consumer to flush incomplete batches periodically (to prevent data loss on system restart).  
- Add security (JWT/OAuth2) for API endpoints.  
- Scale horizontally by running multiple API instances with shared DB or Kafka.

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