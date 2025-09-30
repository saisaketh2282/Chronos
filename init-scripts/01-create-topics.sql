-- Initialize database with required tables and data
-- This script runs automatically when PostgreSQL container starts

-- Create database if not exists (already created by environment variables)
-- CREATE DATABASE chronos_db;

-- Connect to chronos_db
\c chronos_db;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create a simple users table for authentication (if not exists)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER',
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default users for testing
INSERT INTO users (username, password, email, role) VALUES 
    ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin@chronos.com', 'ADMIN'),
    ('user', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'user@chronos.com', 'USER'),
    ('scheduler', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'scheduler@chronos.com', 'SCHEDULER')
ON CONFLICT (username) DO NOTHING;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_enabled ON users(enabled);

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE chronos_db TO chronos_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO chronos_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO chronos_user;

-- Note: The actual job tables will be created by Flyway migrations when the Spring Boot app starts
