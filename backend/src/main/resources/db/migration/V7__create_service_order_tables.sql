CREATE SCHEMA IF NOT EXISTS service_order_schema;

CREATE TABLE service_order_schema.service_orders (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    appointment_id UUID,
    customer_id UUID NOT NULL,
    professional_id UUID NOT NULL,
    service_id UUID NOT NULL,
    service_price NUMERIC(19,2) NOT NULL,
    estimated_duration INTEGER NOT NULL,
    actual_duration INTEGER,
    scheduled_start TIMESTAMP NOT NULL,
    scheduled_end TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    observations TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_service_orders_company
        FOREIGN KEY (company_id)
        REFERENCES company_schema.companies(id),
    CONSTRAINT fk_service_orders_appointment
        FOREIGN KEY (appointment_id)
        REFERENCES schedule_schema.appointments(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_service_orders_customer
        FOREIGN KEY (customer_id)
        REFERENCES customer_schema.customers(id),
    CONSTRAINT fk_service_orders_professional
        FOREIGN KEY (professional_id)
        REFERENCES schedule_schema.employees(id),
    CONSTRAINT fk_service_orders_service
        FOREIGN KEY (service_id)
        REFERENCES service_order_schema.services(id),
    CONSTRAINT chk_service_orders_service_price_positive
        CHECK (service_price > 0),
    CONSTRAINT chk_service_orders_estimated_duration_positive
        CHECK (estimated_duration > 0),
    CONSTRAINT chk_service_orders_actual_duration_non_negative
        CHECK (actual_duration IS NULL OR actual_duration >= 0),
    CONSTRAINT chk_service_orders_schedule_order
        CHECK (scheduled_end > scheduled_start),
    CONSTRAINT chk_service_orders_started_finished_order
        CHECK (started_at IS NULL OR finished_at IS NULL OR finished_at >= started_at),
    CONSTRAINT chk_service_orders_status
        CHECK (status IN ('OPEN', 'CONFIRMED', 'IN_PROGRESS', 'PAUSED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    CONSTRAINT uk_service_orders_appointment
        UNIQUE (appointment_id)
);

CREATE TABLE service_order_schema.service_order_history (
    id UUID PRIMARY KEY,
    service_order_id UUID NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    changed_by UUID NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    observation TEXT,
    CONSTRAINT fk_service_order_history_service_order
        FOREIGN KEY (service_order_id)
        REFERENCES service_order_schema.service_orders(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_service_order_history_previous_status
        CHECK (previous_status IS NULL OR previous_status IN ('OPEN', 'CONFIRMED', 'IN_PROGRESS', 'PAUSED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    CONSTRAINT chk_service_order_history_new_status
        CHECK (new_status IN ('OPEN', 'CONFIRMED', 'IN_PROGRESS', 'PAUSED', 'COMPLETED', 'CANCELLED', 'NO_SHOW'))
);

CREATE INDEX idx_service_orders_company_id ON service_order_schema.service_orders(company_id);
CREATE INDEX idx_service_orders_appointment_id ON service_order_schema.service_orders(appointment_id);
CREATE INDEX idx_service_orders_customer_id ON service_order_schema.service_orders(customer_id);
CREATE INDEX idx_service_orders_professional_id ON service_order_schema.service_orders(professional_id);
CREATE INDEX idx_service_orders_status ON service_order_schema.service_orders(status);
CREATE INDEX idx_service_orders_scheduled_start ON service_order_schema.service_orders(scheduled_start);
