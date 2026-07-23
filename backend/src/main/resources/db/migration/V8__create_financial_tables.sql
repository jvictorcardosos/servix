CREATE SCHEMA IF NOT EXISTS billing_schema;

CREATE TABLE billing_schema.payment_methods (
    id UUID PRIMARY KEY,
    company_id UUID,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_payment_methods_company
        FOREIGN KEY (company_id)
        REFERENCES company_schema.companies(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_payment_methods_name_not_blank
        CHECK (char_length(trim(name)) > 0)
);

CREATE TABLE billing_schema.financial_transactions (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    service_order_id UUID,
    customer_id UUID,
    professional_id UUID,
    service_id UUID,
    transaction_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    discount NUMERIC(19,2) NOT NULL DEFAULT 0,
    surcharge NUMERIC(19,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(19,2) NOT NULL,
    paid_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    due_date DATE NOT NULL,
    payment_date DATE,
    payment_method_id UUID,
    description TEXT,
    external_reference VARCHAR(120),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_financial_transactions_company
        FOREIGN KEY (company_id)
        REFERENCES company_schema.companies(id),
    CONSTRAINT fk_financial_transactions_service_order
        FOREIGN KEY (service_order_id)
        REFERENCES service_order_schema.service_orders(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_financial_transactions_customer
        FOREIGN KEY (customer_id)
        REFERENCES customer_schema.customers(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_financial_transactions_professional
        FOREIGN KEY (professional_id)
        REFERENCES schedule_schema.employees(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_financial_transactions_service
        FOREIGN KEY (service_id)
        REFERENCES service_order_schema.services(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_financial_transactions_payment_method
        FOREIGN KEY (payment_method_id)
        REFERENCES billing_schema.payment_methods(id)
        ON DELETE SET NULL,
    CONSTRAINT chk_financial_transactions_type
        CHECK (transaction_type IN ('RECEIVABLE')),
    CONSTRAINT chk_financial_transactions_status
        CHECK (status IN ('PENDING', 'PARTIALLY_PAID', 'PAID', 'CANCELLED', 'OVERDUE', 'REFUNDED')),
    CONSTRAINT chk_financial_transactions_amount_positive
        CHECK (amount > 0),
    CONSTRAINT chk_financial_transactions_discount_non_negative
        CHECK (discount >= 0),
    CONSTRAINT chk_financial_transactions_surcharge_non_negative
        CHECK (surcharge >= 0),
    CONSTRAINT chk_financial_transactions_total_non_negative
        CHECK (total_amount >= 0),
    CONSTRAINT chk_financial_transactions_paid_non_negative
        CHECK (paid_amount >= 0),
    CONSTRAINT chk_financial_transactions_order
        CHECK (paid_amount <= total_amount)
);

INSERT INTO billing_schema.payment_methods (id, company_id, name, active)
VALUES
    ('11111111-1111-1111-1111-111111111111', NULL, 'Dinheiro', TRUE),
    ('22222222-2222-2222-2222-222222222222', NULL, 'PIX', TRUE),
    ('33333333-3333-3333-3333-333333333333', NULL, 'Cartão de Débito', TRUE),
    ('44444444-4444-4444-4444-444444444444', NULL, 'Cartão de Crédito', TRUE),
    ('55555555-5555-5555-5555-555555555555', NULL, 'Transferência', TRUE),
    ('66666666-6666-6666-6666-666666666666', NULL, 'Boleto', TRUE);

CREATE INDEX idx_payment_methods_company_id ON billing_schema.payment_methods(company_id);
CREATE INDEX idx_payment_methods_active ON billing_schema.payment_methods(active);
CREATE INDEX idx_payment_methods_name ON billing_schema.payment_methods(name);

CREATE INDEX idx_financial_transactions_company_id ON billing_schema.financial_transactions(company_id);
CREATE INDEX idx_financial_transactions_service_order_id ON billing_schema.financial_transactions(service_order_id);
CREATE INDEX idx_financial_transactions_customer_id ON billing_schema.financial_transactions(customer_id);
CREATE INDEX idx_financial_transactions_professional_id ON billing_schema.financial_transactions(professional_id);
CREATE INDEX idx_financial_transactions_service_id ON billing_schema.financial_transactions(service_id);
CREATE INDEX idx_financial_transactions_status ON billing_schema.financial_transactions(status);
CREATE INDEX idx_financial_transactions_due_date ON billing_schema.financial_transactions(due_date);
