CREATE TABLE company_schema.companies (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    documento VARCHAR(32) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_companies_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
CREATE INDEX idx_companies_status ON company_schema.companies(status);

ALTER TABLE auth_schema.users
    ADD CONSTRAINT fk_users_company
    FOREIGN KEY (company_id)
    REFERENCES company_schema.companies(id);
