-- Create jobs table
CREATE TABLE jobs (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    job_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    payload TEXT,
    priority INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    current_retry_count INTEGER DEFAULT 0,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    scheduled_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT
);

-- Create indexes for better performance
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_job_type ON jobs(job_type);
CREATE INDEX idx_jobs_created_by ON jobs(created_by);
CREATE INDEX idx_jobs_scheduled_at ON jobs(scheduled_at);
CREATE INDEX idx_jobs_created_at ON jobs(created_at);
CREATE INDEX idx_jobs_status_scheduled_at ON jobs(status, scheduled_at);
CREATE INDEX idx_jobs_status_priority ON jobs(status, priority);

-- Add check constraints
ALTER TABLE jobs ADD CONSTRAINT chk_job_type CHECK (job_type IN ('ONE_TIME', 'RECURRING', 'BATCH'));
ALTER TABLE jobs ADD CONSTRAINT chk_job_status CHECK (status IN ('SCHEDULED', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED', 'RETRYING'));
ALTER TABLE jobs ADD CONSTRAINT chk_priority CHECK (priority >= 0);
ALTER TABLE jobs ADD CONSTRAINT chk_max_retries CHECK (max_retries >= 0);
ALTER TABLE jobs ADD CONSTRAINT chk_current_retry_count CHECK (current_retry_count >= 0);
