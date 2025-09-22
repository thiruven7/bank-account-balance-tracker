# Bank Account Balance Tracker - Basic Version

A simple application to process credit and debit transactions, track account balances, and submit batched transaction data to an audit system. Includes a React-based UI for real-time balance display.

---

## Features
- Submit and track credit/debit transactions
- Real-time account balance updates
- Batched audit submission
- Simple React UI for balance display

## Tech Stack
- Java 17, Spring Boot, Maven
- H2 database (local), PostgreSQL (production)
- ReactJS, Axios, Bootstrap
- Swagger/OpenAPI for API documentation
- Actuator for metrics

## Quick Start

### API
```bash
cd balance-tracker-api
mvn clean install
mvn spring-boot:run
```

### Producer
```bash
cd transaction-producer
mvn clean install
mvn spring-boot:run
```

### UI
```bash
cd balance-tracker-ui
npm install
npm start
```
Access UI at [http://localhost:3000](http://localhost:3000)

## Key Endpoints
- `POST /api/bankaccount/v1/transactions` → Submit a transaction (credit/debit)
- `GET /api/bankaccount/v1/balance` → Get current account balance
- `POST /api/producer/v1/start` → Start transaction producer
- `POST /api/producer/v1/stop` → Stop transaction producer
- `GET /api/producer/v1/status` → Get producer status

## License
MIT

