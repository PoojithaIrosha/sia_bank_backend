# Banking Microservices Project

SIA is a **Banking System** built with **Spring Boot Microservices** architecture.  
It is designed with scalability, security, and observability in mind.

## 🚀 Microservices Included
1. **Config Server** - Centralized configuration management.
2. **Eureka Server** - Service registry and discovery.
3. **API Gateway** - Routing, JWT validation, resilience, and tracing.
4. **Authentication Service** (upcoming) - Handles user login, token generation, and validation.
5. **Domain Services** - Banking services like Accounts, Transactions, Loans, etc.

---

## 🛠️ Tech Stack
- **Spring Boot 3+**
- **Spring Cloud (Eureka, Config, Gateway)**
- **Spring Security + JWT**
- **Sleuth + Zipkin** (distributed tracing)
- **Docker (Deployments)**

---

## ⚙️ How to Run (Development)
1. Clone the repository.
   ```bash
   git clone https://github.com/your-org/banking-microservices.git
   cd banking-microservices
   ```

2. Start **Config Server**:
   ```bash
   cd config-server
   ./mvnw spring-boot:run
   ```

3. Start **Eureka Server**:
   ```bash
   cd eureka-server
   ./mvnw spring-boot:run
   ```

4. Start **API Gateway**:
   ```bash
   cd api-gateway
   ./mvnw spring-boot:run
   ```

5. Start Authentication and Business Microservices.

---

## 🔑 Security
- All requests go through **API Gateway**.
- Gateway validates **JWT Tokens** using `JwtAuthenticationFilter` and `JwtUtil`.
- Authentication Service will issue JWT tokens upon login.

---

## 📊 Observability
- **Resilience4j** provides fault tolerance with retries and circuit breakers.
- **Spring Cloud Sleuth + Zipkin** integrated for distributed tracing.

---

## 🧑‍💻 Contribution Guide
- Follow standard **Git branching strategy** (`feature/*`, `bugfix/*`, `hotfix/*`).
- Write meaningful commit messages.
- Ensure code is formatted with project standards.
- Add **unit tests** for new functionality.

---

## 📂 Project Structure
```
banking-microservices/
│── config-server/
│── eureka-server/
│── api-gateway/
|── other-microservices/
└── README.md
```

---

## 📌 Notes for Developers
- Ports:
  - Config Server → `8888`
  - Eureka Server → `8761`
  - API Gateway → `8080`
- Configurations are externalized in Config Server.
- Ensure services register with Eureka on startup.
