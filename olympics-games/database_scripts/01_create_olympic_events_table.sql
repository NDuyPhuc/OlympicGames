USE olympicgames;
-- File: database_scripts/01_create_olympic_events_table.sql
CREATE TABLE IF NOT EXISTS olympic_events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    event_name VARCHAR(100) NOT NULL,
    YEAR INT NOT NULL,
    event_type VARCHAR(20) NOT NULL, -- 'Summer' or 'Winter'
    table_name_in_db VARCHAR(100) NOT NULL UNIQUE -- Tên bảng huy chương tương ứng
);