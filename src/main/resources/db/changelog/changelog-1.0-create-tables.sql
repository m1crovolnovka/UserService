CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       surname VARCHAR(100) NOT NULL,
                       birth_date DATE NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

CREATE TABLE payment_cards (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id UUID NOT NULL,
                               number VARCHAR(20) NOT NULL,
                               holder VARCHAR(150) NOT NULL,
                               expiration_date DATE NOT NULL,
                               active BOOLEAN DEFAULT TRUE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_cards_user_id ON payment_cards(user_id);
CREATE INDEX idx_cards_number ON payment_cards(number);
