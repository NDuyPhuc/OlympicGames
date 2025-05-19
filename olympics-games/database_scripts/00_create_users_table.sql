CREATE DATABASE olympicgames;
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
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'admin@example.com', 'ADMIN');
