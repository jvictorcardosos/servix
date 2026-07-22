CREATE TABLE auth_schema.profiles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE,
    CONSTRAINT chk_profiles_name
        CHECK (name IN ('ADMIN', 'GESTOR', 'OPERADOR'))
);

CREATE TABLE auth_schema.users (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT chk_users_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
CREATE INDEX idx_users_company_id ON auth_schema.users(company_id);

CREATE TABLE auth_schema.user_profiles (
    user_id UUID NOT NULL,
    profile_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, profile_id),
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES auth_schema.users(id),
    CONSTRAINT fk_user_profiles_profile FOREIGN KEY (profile_id) REFERENCES auth_schema.profiles(id)
);

CREATE TABLE auth_schema.refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES auth_schema.users(id)
);
CREATE INDEX idx_refresh_tokens_user_id ON auth_schema.refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON auth_schema.refresh_tokens(expires_at);

INSERT INTO auth_schema.profiles(name)
VALUES ('ADMIN'),
       ('GESTOR'),
       ('OPERADOR');
