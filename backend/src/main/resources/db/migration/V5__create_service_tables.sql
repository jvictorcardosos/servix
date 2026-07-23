CREATE TABLE service_order_schema.services (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL,
    price NUMERIC(19,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_services_company
        FOREIGN KEY (company_id)
        REFERENCES company_schema.companies(id),
    CONSTRAINT chk_services_name_not_blank
        CHECK (char_length(trim(name)) > 0),
    CONSTRAINT chk_services_duration_positive
        CHECK (duration_minutes > 0),
    CONSTRAINT chk_services_price_positive
        CHECK (price > 0),
    CONSTRAINT uk_services_company_name
        UNIQUE (company_id, name)
);

CREATE INDEX idx_services_company_id ON service_order_schema.services(company_id);
CREATE INDEX idx_services_active ON service_order_schema.services(active);
CREATE INDEX idx_services_name ON service_order_schema.services(name);
