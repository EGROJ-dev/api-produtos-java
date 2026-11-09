CREATE TABLE IF NOT EXISTS produto (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nome    VARCHAR(100)  NOT NULL,
    preco   DECIMAL(12,2) NOT NULL CHECK (preco >= 0),
    estoque INT           NOT NULL CHECK (estoque >= 0)
);
