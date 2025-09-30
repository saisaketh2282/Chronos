# Chronos - Job Scheduler System

A scalable job scheduling system in Java that supports one-time & recurring jobs with retry mechanisms, monitoring, and email notifications.

## 🎯 Objective

Chronos is a comprehensive job scheduling system that provides:
- **One-time & Recurring Jobs**: Support for both immediate and cron-based job scheduling
- **Retry Mechanism**: Failed jobs are automatically retried using Kafka with exponential backoff
- **Real-time Monitoring**: Job execution is monitored and logged to Splunk
- **Email Notifications**: Automatic email notifications upon job completion or failure
- **JWT Authentication**: Secure API access with JWT-based authentication

## 🛠️ Tech Stack

- **Language & Core**: Java 21 with Spring Boot 3.5.6
- **API Layer**: REST APIs with Spring Web
- **Database**: PostgreSQL with JPA/Hibernate
- **Messaging**: Apache Kafka for failure events and retry pipeline
- **Monitoring**: Splunk for real-time job lifecycle logs
- **Notifications**: JavaMail API for email notifications
- **Authentication**: JWT-based authentication
- **Scheduling**: Spring's built-in scheduling with custom thread pools

## 🏗️ Architecture

### High-Level Design (HLD)

The system is organized into five main layers:

1. **User Interaction Layer**
   - REST APIs for job management (create, update, cancel, check status)
   - JWT authentication for secure access
   - Job definitions stored in PostgreSQL

2. **Scheduler Layer**
   - **One-time Scheduler**: Executes jobs once at scheduled time
   - **Recurring Scheduler**: Handles cron-like periodic jobs
   - **Retry Scheduler**: Consumes failed jobs from Kafka and retries with backoff

3. **Execution Layer**
   - **Executor Service**: Thread pool-based concurrent job execution
   - Real-time logging to Splunk with status transitions
   - Job lifecycle tracking (Scheduled → Running → Success/Failure)

4. **Failure Handling**
   - Failed jobs publish events to Kafka
   - Retry scheduler consumes events and retries with exponential backoff
   - Dead letter queue for jobs exceeding max retries

5. **Notification Layer**
   - Email summaries sent upon job completion
   - System alerts for failures and stuck jobs
   - HTML-formatted notifications with job details

## 🚀 Getting Started

### Prerequisites

- Java 21+
- PostgreSQL 12+
- Apache Kafka 2.8+
- Splunk (optional, for monitoring)
- SMTP server (for email notifications)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Chronos
   ```

2. **Configure Database**
   ```sql
   CREATE DATABASE chronos_db;
   CREATE USER chronos_user WITH PASSWORD 'chronos_password';
   GRANT ALL PRIVILEGES ON DATABASE chronos_db TO chronos_user;
   ```

3. **Configure Kafka Topics**
   ```bash
   # Create required Kafka topics
   kafka-topics --create --topic chronos.failure.events --bootstrap-server localhost:9092
   kafka-topics --create --topic chronos.retry.pipeline --bootstrap-server localhost:9092
   kafka-topics --create --topic chronos.dead.letter.queue --bootstrap-server localhost:9092
   kafka-topics --create --topic chronos.job.events --bootstrap-server localhost:9092
   ```

4. **Update Configuration**
   Edit `src/main/resources/application.properties`:
   ```properties
   # Database Configuration
   spring.datasource.url=jdbc:postgresql://localhost:5432/chronos_db
   spring.datasource.username=chronos_user
   spring.datasource.password=chronos_password
   
   # Kafka Configuration
   spring.kafka.bootstrap-servers=localhost:9092
   
   # Splunk Configuration
   splunk.host=localhost
   splunk.port=8089
   splunk.username=admin
   splunk.password=changeme
   
   # Email Configuration
   spring.mail.host=smtp.gmail.com
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   ```

5. **Run the Application**
   ```bash
   ./gradlew bootRun
   ```

## 📚 API Documentation

### Authentication

**Login**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "username": "admin"
}
```

### Job Management

**Create One-time Job**
```http
POST /api/jobs
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Data Processing Job",
  "description": "Process customer data",
  "jobType": "ONE_TIME",
  "payload": "{\"data\": \"customer_data.csv\"}",
  "scheduledAt": "2024-01-15T10:30:00",
  "priority": 1,
  "maxRetries": 3
}
```

**Create Recurring Job**
```http
POST /api/jobs
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Daily Report",
  "description": "Generate daily sales report",
  "jobType": "RECURRING",
  "payload": "{\"reportType\": \"sales\"}",
  "cronExpression": "0 0 9 * * ?",
  "priority": 2,
  "maxRetries": 2
}
```

**Get All Jobs**
```http
GET /api/jobs?page=0&size=10&sortBy=createdAt&sortDir=desc
Authorization: Bearer <token>
```

**Get Job by ID**
```http
GET /api/jobs/{id}?includeLogs=true
Authorization: Bearer <token>
```

**Cancel Job**
```http
POST /api/jobs/{id}/cancel
Authorization: Bearer <token>
```

**Get Job Logs**
```http
GET /api/jobs/{id}/logs?page=0&size=20
Authorization: Bearer <token>
```

**Get Job Statistics**
```http
GET /api/jobs/statistics
Authorization: Bearer <token>
```

### Admin Operations

**Get Stuck Jobs**
```http
GET /api/admin/jobs/stuck?timeoutMinutes=30
Authorization: Bearer <token>
```

**Reset Stuck Jobs**
```http
POST /api/admin/jobs/stuck/reset?timeoutMinutes=30
Authorization: Bearer <token>
```

**System Statistics**
```http
GET /api/admin/statistics
Authorization: Bearer <token>
```

## 🔧 Configuration

### Thread Pool Configuration
```properties
chronos.executor.core-pool-size=10
chronos.executor.max-pool-size=50
chronos.executor.queue-capacity=100
chronos.executor.thread-name-prefix=chronos-executor-
```

### Job Configuration
```properties
chronos.job.max-retries=3
chronos.job.retry-delay=5000
chronos.job.cleanup-days=30
```

### JWT Configuration
```properties
jwt.secret=your-secret-key
jwt.expiration=86400000
```

## 📊 Monitoring & Observability

### Splunk Integration

Chronos automatically logs job events to Splunk:
- Job lifecycle events (scheduled, started, completed, failed)
- Execution metrics and performance data
- System alerts and error messages
- Retry attempts and dead letter queue events

### Health Checks

**Application Health**
```http
GET /api/health
```

**Readiness Check**
```http
GET /api/health/ready
```

**Liveness Check**
```http
GET /api/health/live
```

## 🔄 Job Flow

1. **Job Submission** → Stored in DB → Scheduler picks it up
2. **Job Running** → Logs pushed to Splunk in real-time
3. **On Failure** → Event → Kafka → Retry Scheduler
4. **On Completion** → Splunk updated → Email sent to user

## 📧 Email Notifications

The system sends HTML-formatted email notifications for:
- ✅ Job completion with execution details
- ❌ Job failure with error messages and retry information
- 📊 Batch job execution summaries
- 🚨 System alerts for stuck jobs and errors

## 🔐 Security

- JWT-based authentication for all API endpoints
- Role-based access control (USER, ADMIN, SCHEDULER)
- CORS configuration for cross-origin requests
- Secure password encoding with BCrypt

## 🧪 Testing

Run the test suite:
```bash
./gradlew test
```

## 📈 Performance

- Configurable thread pools for optimal resource utilization
- Asynchronous job execution with CompletableFuture
- Database connection pooling
- Kafka producer/consumer optimization
- Efficient database queries with proper indexing

## 🚨 Troubleshooting

### Common Issues

1. **Database Connection Issues**
   - Verify PostgreSQL is running
   - Check database credentials in application.properties
   - Ensure database exists and user has proper permissions

2. **Kafka Connection Issues**
   - Verify Kafka is running on configured port
   - Check if required topics exist
   - Verify consumer group configuration

3. **Splunk Connection Issues**
   - Verify Splunk is accessible
   - Check credentials and index configuration
   - System will continue without Splunk (logs to console)

4. **Email Delivery Issues**
   - Verify SMTP configuration
   - Check email credentials and app passwords
   - Ensure firewall allows SMTP connections

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review the API documentation

---

**Chronos Job Scheduler** - Reliable, scalable, and monitored job execution for modern applications.
