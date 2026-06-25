-- Performance Optimization Migration

-- Appointments search indexes
CREATE INDEX IF NOT EXISTS idx_appointments_doctor_date ON appointments (doctor_id, appointment_date);
CREATE INDEX IF NOT EXISTS idx_appointments_patient_id ON appointments (patient_id);
CREATE INDEX IF NOT EXISTS idx_appointments_status ON appointments (status);

-- Doctor search indexes
CREATE INDEX IF NOT EXISTS idx_doctors_specialization ON doctors (specialization);
CREATE INDEX IF NOT EXISTS idx_doctors_email ON doctors (email);

-- Patient search indexes
CREATE INDEX IF NOT EXISTS idx_patients_email ON patients (email);

-- User search indexes
CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);

-- Soft delete indexes (since almost all queries filter by deleted_at IS NULL)
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users (deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_doctors_deleted_at ON doctors (deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_patients_deleted_at ON patients (deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_appointments_deleted_at ON appointments (deleted_at) WHERE deleted_at IS NULL;

-- Token cleanup performance
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
CREATE INDEX IF NOT EXISTS idx_email_verification_tokens_expires_at ON email_verification_tokens (expires_at);
