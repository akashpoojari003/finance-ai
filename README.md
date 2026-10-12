# AI Finance Manager

Monorepo for the AI Finance Manager platform.

| Folder     | Description                                  |
|------------|----------------------------------------------|
| `backend/` | Java 21, Spring Boot 3.3, Maven, PostgreSQL  |
| `frontend/`| React 18, Vite, Tailwind, Recharts           |
| `infra/`   | Docker, Jenkins, Prometheus, Grafana configs |

## Prerequisites

- JDK 21
- Maven 3.9+
- Node.js 20+ and npm
- Docker and Docker Compose
- PostgreSQL 15+ (if not using Docker)

## Setup

### 1. Clone
```bash
git clone <REPO_URL>
cd ai-finance-manager
```

### 2. Environment variables
```bash
cp infra/.env.example infra/.env
# TODO: fill in real values in infra/.env (never commit this file)
```

### 3. Database
TODO: start PostgreSQL (docker compose command placeholder)

### 4. Backend
```bash
cd backend
# TODO: mvn spring-boot:run
```
- Base package: `com.financeai`
- API base path: `/api`
- Timezone: `Asia/Kolkata`

### 5. Frontend
```bash
cd frontend
# TODO: npm install
# TODO: npm run dev
```

### 6. Run everything with Docker
TODO: docker compose up command placeholder

## Testing
- Backend: TODO
- Frontend: TODO

## CI/CD
TODO: Jenkins pipeline description

## Monitoring and Logging
- Prometheus: TODO
- Grafana: TODO
- Coralogix: TODO

## API Docs
TODO

## License
TODO