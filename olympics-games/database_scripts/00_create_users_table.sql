USE olympicgames;

-- File: database_scripts/00_create_users_table.sql
CREATE TABLE IF NOT EXISTS Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    hashed_password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    ROLE VARCHAR(20) NOT NULL -- Ví dụ: 'ADMIN', 'STAFF'
);

INSERT INTO Users (username, hashed_password, email, ROLE) VALUES 
('admin', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', 'admin@example.com', 'ADMIN');