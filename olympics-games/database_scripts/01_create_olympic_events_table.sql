USE olympicgames;

DROP TABLE IF EXISTS olympic_events;
CREATE TABLE `olympic_events` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `event_name` VARCHAR(255) NOT NULL,
  `year` INT NOT NULL,
  `event_type` VARCHAR(50), -- e.g., Summer, Winter
  `table_name_in_db` VARCHAR(255) NOT NULL UNIQUE 
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;