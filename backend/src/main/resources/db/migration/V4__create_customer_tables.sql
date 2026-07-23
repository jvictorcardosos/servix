CREATE TABLE customer_schema.customers (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    nome VARCHAR(150) NOT NULL,
    cpf_cnpj VARCHAR(32) NOT NULL,
    email VARCHAR(150) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    telefone_secundario VARCHAR(20),
    cep VARCHAR(8) NOT NULL,
    logradouro VARCHAR(180) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    complemento VARCHAR(100),
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    estado VARCHAR(2) NOT NULL,
    observacoes TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_customers_company
        FOREIGN KEY (company_id)
        REFERENCES company_schema.companies(id),
    CONSTRAINT chk_customers_estado
        CHECK (char_length(estado) = 2),
    CONSTRAINT uk_customers_company_cpf_cnpj
        UNIQUE (company_id, cpf_cnpj),
    CONSTRAINT uk_customers_company_email
        UNIQUE (company_id, email)
);

CREATE INDEX idx_customers_company_id ON customer_schema.customers(company_id);
CREATE INDEX idx_customers_company_nome ON customer_schema.customers(company_id, nome);
CREATE INDEX idx_customers_company_cpf_cnpj ON customer_schema.customers(company_id, cpf_cnpj);
CREATE INDEX idx_customers_company_email ON customer_schema.customers(company_id, email);
CREATE INDEX idx_customers_company_telefone ON customer_schema.customers(company_id, telefone);
CREATE INDEX idx_customers_company_ativo ON customer_schema.customers(company_id, ativo);
