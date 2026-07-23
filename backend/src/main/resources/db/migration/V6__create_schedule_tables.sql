CREATE SCHEMA IF NOT EXISTS schedule_schema;

CREATE TABLE schedule_schema.employees (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_employees_company
        FOREIGN KEY (company_id)
        REFERENCES company_schema.companies(id),
    CONSTRAINT chk_employees_name_not_blank
        CHECK (char_length(trim(name)) > 0),
    CONSTRAINT chk_employees_email_not_blank
        CHECK (char_length(trim(email)) > 0),
    CONSTRAINT uk_employees_company_email
        UNIQUE (company_id, email)
);

CREATE TABLE schedule_schema.work_schedules (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL,
    day_of_week INTEGER NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_work_schedules_employee
        FOREIGN KEY (employee_id)
        REFERENCES schedule_schema.employees(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_work_schedules_day_of_week
        CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT chk_work_schedules_time_order
        CHECK (end_time > start_time)
);

CREATE TABLE schedule_schema.appointments (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    service_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_appointments_company
        FOREIGN KEY (company_id)
        REFERENCES company_schema.companies(id),
    CONSTRAINT fk_appointments_customer
        FOREIGN KEY (customer_id)
        REFERENCES customer_schema.customers(id),
    CONSTRAINT fk_appointments_service
        FOREIGN KEY (service_id)
        REFERENCES service_order_schema.services(id),
    CONSTRAINT fk_appointments_employee
        FOREIGN KEY (employee_id)
        REFERENCES schedule_schema.employees(id),
    CONSTRAINT chk_appointments_time_order
        CHECK (end_time > start_time),
    CONSTRAINT chk_appointments_status
        CHECK (status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW'))
);

CREATE INDEX idx_employees_company_id ON schedule_schema.employees(company_id);
CREATE INDEX idx_employees_active ON schedule_schema.employees(active);
CREATE INDEX idx_employees_name ON schedule_schema.employees(name);

CREATE INDEX idx_appointments_company_id ON schedule_schema.appointments(company_id);
CREATE INDEX idx_appointments_customer_id ON schedule_schema.appointments(customer_id);
CREATE INDEX idx_appointments_service_id ON schedule_schema.appointments(service_id);
CREATE INDEX idx_appointments_employee_id ON schedule_schema.appointments(employee_id);
CREATE INDEX idx_appointments_appointment_date ON schedule_schema.appointments(appointment_date);
CREATE INDEX idx_appointments_status ON schedule_schema.appointments(status);
