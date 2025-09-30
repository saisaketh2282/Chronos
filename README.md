# 🕒 Chronos - Job Scheduler & Management System

A comprehensive job scheduling and management system built with Spring Boot and React, featuring real-time monitoring, failure handling, and scalable architecture.

## 🚀 Features

- **Job Management**: Create, update, delete, and monitor scheduled jobs
- **Real-time Monitoring**: Live dashboard with job execution status and logs
- **Failure Handling**: Automatic retry mechanisms and dead letter queue
- **Scalable Architecture**: Built with Spring Boot, Kafka, and PostgreSQL
- **Modern UI**: React-based frontend with responsive design
- **Docker Support**: Easy deployment with Docker Compose
- **Security**: JWT-based authentication and role-based access control

## 🏗️ Architecture

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.x with Java 17+
- **Database**: PostgreSQL with Flyway migrations
- **Message Queue**: Apache Kafka for event-driven architecture
- **Security**: Spring Security with JWT
- **Scheduling**: Spring's @Scheduled with custom job executor

### Frontend (React)
- **Framework**: React 18 with modern hooks
- **Styling**: Tailwind CSS for responsive design
- **State Management**: Context API for authentication
- **HTTP Client**: Axios for API communication

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Database**: PostgreSQL 16
- **Message Broker**: Apache Kafka with Zookeeper
- **Monitoring**: Optional Splunk integration

## 📋 Prerequisites

- Java 17 or higher
- Node.js 16 or higher
- Docker Desktop
- Git

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <your-github-repo-url>
cd Chronos
```

### 2. Start Infrastructure Services
```bash
# Start PostgreSQL, Kafka, and Zookeeper
docker-compose up -d

# Optional: Start with monitoring (Splunk)
docker-compose --profile monitoring up -d
```

### 3. Start the Backend
```bash
cd Chronos
./gradlew bootRun
```

### 4. Start the Frontend
```bash
cd frontend
npm install
npm start
```

### 5. Access the Application
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Kafka UI**: http://localhost:8081

## 🔧 Configuration

### Database Configuration
The application uses PostgreSQL with the following default settings:
- **Host**: localhost:5432
- **Database**: chronos_db
- **Username**: chronos_user
- **Password**: chronos_password

### Kafka Configuration
- **Bootstrap Servers**: localhost:9092
- **Topics**:
  - `chronos.job.events` - Job execution events
  - `chronos.failure.events` - Job failure events
  - `chronos.retry.pipeline` - Retry queue
  - `chronos.dead.letter.queue` - Failed jobs

### Default Users
- **Admin**: admin / admin123
- **User**: user / user123
- **Scheduler**: scheduler / scheduler123

## 📁 Project Structure

```
Chronos/
├── Chronos/                    # Spring Boot Backend
│   ├── src/main/java/
│   │   └── com/Prod/Chronos/
│   │       ├── config/         # Configuration classes
│   │       ├── controller/     # REST controllers
│   │       ├── entity/         # JPA entities
│   │       ├── repository/     # Data repositories
│   │       ├── security/       # Security configuration
│   │       └── service/        # Business logic
│   └── src/main/resources/
│       ├── application.properties
│       └── db/migration/       # Flyway migrations
├── frontend/                   # React Frontend
│   ├── src/
│   │   ├── components/         # React components
│   │   ├── pages/             # Page components
│   │   ├── services/          # API services
│   │   └── contexts/          # React contexts
│   └── public/
├── docker-compose.yml          # Infrastructure setup
├── init-scripts/              # Database initialization
└── setup-guide.md            # Detailed setup instructions
```

## 🛠️ Development

### Backend Development
```bash
cd Chronos
./gradlew bootRun
```

### Frontend Development
```bash
cd frontend
npm install
npm start
```

### Running Tests
```bash
# Backend tests
cd Chronos
./gradlew test

# Frontend tests
cd frontend
npm test
```

## 🐳 Docker Deployment

### Production Build
```bash
# Build backend
cd Chronos
./gradlew bootJar

# Build frontend
cd frontend
npm run build

# Start all services
docker-compose up -d
```

### Environment Variables
Create a `.env` file for production configuration:
```env
POSTGRES_PASSWORD=your_secure_password
KAFKA_BOOTSTRAP_SERVERS=your_kafka_servers
JWT_SECRET=your_jwt_secret
```

## 📊 Monitoring & Logging

### Application Logs
- Backend logs: Available in console and configured log files
- Frontend logs: Browser console and network tab

### Infrastructure Monitoring
- **Kafka UI**: http://localhost:8081 - Monitor topics and messages
- **Splunk**: http://localhost:8000 - Centralized logging (optional)

### Health Checks
- **Backend Health**: GET http://localhost:8080/actuator/health
- **Database**: Automatic health checks in Docker Compose
- **Kafka**: Built-in health checks

## 🔒 Security

### Authentication
- JWT-based authentication
- Role-based access control (RBAC)
- Secure password hashing with BCrypt

### API Security
- CORS configuration
- Request validation
- SQL injection prevention with JPA

## 🚨 Troubleshooting

### Common Issues

1. **Port Conflicts**
   ```bash
   # Check port usage
   netstat -ano | findstr :8080
   netstat -ano | findstr :5432
   ```

2. **Database Connection Issues**
   ```bash
   # Reset database
   docker-compose down -v
   docker-compose up -d postgres
   ```

3. **Kafka Connection Issues**
   ```bash
   # Restart Kafka
   docker-compose restart kafka
   ```

4. **Frontend Build Issues**
   ```bash
   # Clear cache and reinstall
   cd frontend
   rm -rf node_modules package-lock.json
   npm install
   ```

- [ ] Job templates and workflows

---

**Happy Scheduling! 🕒✨**
