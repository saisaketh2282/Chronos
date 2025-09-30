# 🐳 Docker Setup Guide for Chronos

This guide will help you set up Kafka and PostgreSQL using Docker Desktop for the Chronos job scheduler project.

## 📋 Prerequisites

- ✅ Docker Desktop installed and running
- ✅ Git (to clone the project)

## 🚀 Quick Start

### 1. Start the Services

Open your terminal/command prompt in the project root directory and run:

```bash
# Start all services (PostgreSQL, Kafka, Zookeeper)
docker-compose up -d

# To also start Splunk (optional monitoring):
docker-compose --profile monitoring up -d
```

### 2. Verify Services are Running

```bash
# Check if all containers are running
docker-compose ps

# Check logs if needed
docker-compose logs postgres
docker-compose logs kafka
```

### 3. Access the Services

| Service | URL | Credentials |
|---------|-----|-------------|
| **PostgreSQL** | `localhost:5432` | Username: `chronos_user`, Password: `chronos_password`, Database: `chronos_db` |
| **Kafka** | `localhost:9092` | No authentication |
| **Kafka UI** | http://localhost:8080 | No authentication |
| **Splunk** | http://localhost:8000 | Username: `admin`, Password: `changeme` |

## 🔧 Detailed Setup Steps

### Step 1: Clone and Navigate to Project
```bash
cd C:\Users\saisa\Downloads\Chronos
```

### Step 2: Start Docker Services
```bash
# Start core services
docker-compose up -d postgres zookeeper kafka kafka-topics

# Wait for services to be healthy (about 30-60 seconds)
docker-compose ps
```

### Step 3: Verify Database Connection
```bash
# Test PostgreSQL connection
docker exec -it chronos-postgres psql -U chronos_user -d chronos_db -c "\dt"

# You should see the users table and any tables created by Flyway
```

### Step 4: Verify Kafka Topics
```bash
# List Kafka topics
docker exec -it chronos-kafka kafka-topics --bootstrap-server localhost:9092 --list

# You should see:
# - chronos.failure.events
# - chronos.retry.pipeline
# - chronos.dead.letter.queue
# - chronos.job.events
```

### Step 5: Start Kafka UI (Optional)
```bash
# Access Kafka UI at http://localhost:8080
docker-compose up -d kafka-ui
```

## 🛠️ Troubleshooting

### Common Issues:

#### 1. Port Already in Use
```bash
# Check what's using the ports
netstat -ano | findstr :5432
netstat -ano | findstr :9092

# Stop conflicting services or change ports in docker-compose.yml
```

#### 2. Services Not Starting
```bash
# Check logs
docker-compose logs

# Restart services
docker-compose down
docker-compose up -d
```

#### 3. Database Connection Issues
```bash
# Reset database
docker-compose down -v  # This removes volumes (data)
docker-compose up -d postgres
```

#### 4. Kafka Connection Issues
```bash
# Restart Kafka
docker-compose restart kafka

# Check Kafka health
docker exec -it chronos-kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

## 📊 Service Management

### Start Services
```bash
# Start all services
docker-compose up -d

# Start specific services
docker-compose up -d postgres kafka
```

### Stop Services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes (deletes data)
docker-compose down -v
```

### View Logs
```bash
# All services
docker-compose logs

# Specific service
docker-compose logs postgres
docker-compose logs kafka
```

### Restart Services
```bash
# Restart all
docker-compose restart

# Restart specific service
docker-compose restart postgres
```

## 🔍 Health Checks

### PostgreSQL Health
```bash
# Check if PostgreSQL is ready
docker exec -it chronos-postgres pg_isready -U chronos_user -d chronos_db

# Connect to database
docker exec -it chronos-postgres psql -U chronos_user -d chronos_db
```

### Kafka Health
```bash
# Check Kafka broker
docker exec -it chronos-kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# List topics
docker exec -it chronos-kafka kafka-topics --bootstrap-server localhost:9092 --list
```

## 🎯 Next Steps

After setting up Docker services:

1. **Start the Backend**:
   ```bash
   cd Chronos
   ./gradlew bootRun
   ```

2. **Start the Frontend**:
   ```bash
   cd frontend
   npm install
   npm start
   ```

3. **Access the Application**:
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Kafka UI: http://localhost:8080 (Kafka management)
   - Splunk: http://localhost:8000 (if enabled)

## 📝 Default Credentials

### Database
- **Username**: `chronos_user`
- **Password**: `chronos_password`
- **Database**: `chronos_db`

### Application Users
- **Admin**: `admin` / `admin123`
- **User**: `user` / `user123`
- **Scheduler**: `scheduler` / `scheduler123`

### Splunk (if enabled)
- **Username**: `admin`
- **Password**: `changeme`

## 🔧 Configuration

The services are configured to work with the Chronos application out of the box. If you need to modify settings:

1. **Database**: Edit `docker-compose.yml` under the `postgres` service
2. **Kafka**: Edit `docker-compose.yml` under the `kafka` service
3. **Application**: Edit `Chronos/src/main/resources/application.properties`

## 📞 Support

If you encounter issues:

1. Check the logs: `docker-compose logs`
2. Verify services are healthy: `docker-compose ps`
3. Restart services: `docker-compose restart`
4. Check this guide for common solutions

Happy coding! 🚀
