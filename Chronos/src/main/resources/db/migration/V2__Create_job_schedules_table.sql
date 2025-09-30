-- Create job_schedules table
CREATE TABLE job_schedules (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    schedule_type VARCHAR(50) NOT NULL,
    cron_expression VARCHAR(255),
    execution_time TIMESTAMP,
    timezone VARCHAR(50) DEFAULT 'UTC',
    is_active BOOLEAN DEFAULT true,
    next_execution TIMESTAMP,
    last_execution TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_job_schedules_job_id FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_job_schedules_job_id ON job_schedules(job_id);
CREATE INDEX idx_job_schedules_schedule_type ON job_schedules(schedule_type);
CREATE INDEX idx_job_schedules_is_active ON job_schedules(is_active);
CREATE INDEX idx_job_schedules_next_execution ON job_schedules(next_execution);
CREATE INDEX idx_job_schedules_active_next_execution ON job_schedules(is_active, next_execution);

-- Add check constraints
ALTER TABLE job_schedules ADD CONSTRAINT chk_schedule_type CHECK (schedule_type IN ('ONE_TIME', 'CRON', 'INTERVAL'));
ALTER TABLE job_schedules ADD CONSTRAINT chk_cron_or_execution_time CHECK (
    (schedule_type = 'CRON' AND cron_expression IS NOT NULL) OR
    (schedule_type = 'ONE_TIME' AND execution_time IS NOT NULL) OR
    (schedule_type = 'INTERVAL')
);
