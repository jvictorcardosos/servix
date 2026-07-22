CREATE TABLE company_schema.companies (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    documento VARCHAR(32) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

ALTER TABLE auth_schema.users
    ADD CONSTRAINT fk_users_company
    FOREIGN KEY (company_id)
    REFERENCES company_schema.companies(id);
