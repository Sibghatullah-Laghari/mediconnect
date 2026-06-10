CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100)  NOT NULL,
    email           VARCHAR(255)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255)  NOT NULL,
    role            VARCHAR(20)   NOT NULL,
    refresh_token   TEXT,
    email_verified  BOOLEAN       NOT NULL DEFAULT FALSE,
    verification_code VARCHAR(50),
    login_attempts  INT           NOT NULL DEFAULT 0,
    last_failed_login_time TIMESTAMP,
    account_locked  BOOLEAN       NOT NULL DEFAULT FALSE,
    password_reset_token VARCHAR(255),
    password_reset_expiry TIMESTAMP,
    is_deleted      BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE patients (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100)  NOT NULL,
    email           VARCHAR(255)  NOT NULL UNIQUE,
    phone           VARCHAR(20)   NOT NULL,
    date_of_birth   DATE          NOT NULL,
    gender          VARCHAR(20)   NOT NULL,
    address         VARCHAR(500),
    user_id         BIGINT REFERENCES users(id),
    is_deleted      BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE doctors (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100)  NOT NULL,
    gender          VARCHAR(20)   NOT NULL,
    specialization  VARCHAR(100)  NOT NULL,
    phone           VARCHAR(20)   NOT NULL,
    email           VARCHAR(255)  NOT NULL UNIQUE,
    fee             NUMERIC(10,2) NOT NULL,
    experience      INT           NOT NULL,
    user_id         BIGINT REFERENCES users(id),
    is_deleted      BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE appointments (
    id                BIGSERIAL PRIMARY KEY,
    status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    reason            VARCHAR(500)  NOT NULL,
    appointment_date  DATE          NOT NULL,
    appointment_time  TIME          NOT NULL,
    patient_id        BIGINT        NOT NULL REFERENCES patients(id),
    doctor_id         BIGINT        NOT NULL REFERENCES doctors(id),
    is_deleted        BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMP,
    created_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_patients_user_id ON patients(user_id);
CREATE INDEX idx_doctors_user_id ON doctors(user_id);
CREATE INDEX idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX idx_appointments_doctor_id ON appointments(doctor_id);
CREATE INDEX idx_appointments_doctor_slot ON appointments(doctor_id, appointment_date, appointment_time);
