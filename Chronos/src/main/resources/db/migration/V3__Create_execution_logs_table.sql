-- Create execution_logs table
CREATE TABLE execution_logs (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    log_level VARCHAR(20) NOT NULL,
    message TEXT,
    details TEXT,
    execution_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration_ms BIGINT,
    thread_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_execution_logs_job_id FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_execution_logs_job_id ON execution_logs(job_id);
CREATE INDEX idx_execution_logs_log_level ON execution_logs(log_level);
CREATE INDEX idx_execution_logs_created_at ON execution_logs(created_at);
CREATE INDEX idx_execution_logs_job_id_created_at ON execution_logs(job_id, created_at);
CREATE INDEX idx_execution_logs_thread_name ON execution_logs(thread_name);

-- Add check constraints
ALTER TABLE execution_logs ADD CONSTRAINT chk_log_level CHECK (log_level IN ('INFO', 'WARN', 'ERROR', 'DEBUG', 'TRACE'));
ALTER TABLE execution_logs ADD CONSTRAINT chk_duration_ms CHECK (duration_ms >= 0);
