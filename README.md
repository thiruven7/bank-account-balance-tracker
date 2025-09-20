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

## Balance Tracker API

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
git clone https://github.com/<your-username>/bank-account-balance-tracker.git
   cd bank-account-balance-tracker/balance-tracker-api
mvn clean install
mvn spring-boot:run
```
2. **Build the project**:
```bash
mvn clean install
```
3. **Run the application**:
```bash
mvn spring-boot:run
```
