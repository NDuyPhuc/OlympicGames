
# Olympic Games Medal Analyzer

## 1. Project Description

This Java desktop application, "Olympic Games Medal Analyzer," allows users (administrators and staff) to log in, manage Olympic Games event data and medal records, and visualize statistical insights through various charts. The system connects to a MySQL database, implements Object-Oriented Programming (OOP) principles, utilizes Java Collections for data handling, and employs JFreeChart for data visualization.

This project was developed as the final assignment for the Object-Oriented Programming with Java (ITE23005) course.

## 2. Prerequisites

Before running this application, ensure you have the following installed:

*   **Java Development Kit (JDK):** Version 20 or higher (as per `pom.xml`).
    *   *Download JavaFX SDK separately if running directly from Eclipse and encountering module issues. For this project, JavaFX SDK 21.0.7 was used. You can download it from [GluonHQ](https://gluonhq.com/products/javafx/).*
*   **Apache Maven:** To build the project and manage dependencies.
*   **MySQL Server:** Version 8.0 or compatible.
*   **MySQL Client:** SQLyog (as specified) or any other MySQL client (e.g., MySQL Workbench, DBeaver) for database setup.
*   **Eclipse IDE:** The project was developed and tested using Eclipse IDE.
*   **Git:** To clone the repository.

## 3. Database Setup

The application requires a MySQL database named `olympicgames`.

1.  **Create the Database:**
    Using SQLyog or your preferred MySQL client, execute the following command:
    ```sql
    CREATE DATABASE olympicgames;
    ```

2.  **Run SQL Scripts:**
    Execute the following SQL scripts located in the `database_scripts` folder **in the specified order**:
    1.  `00_create_users_table.sql` - Creates the `Users` table and inserts a default admin.
    2.  `01_create_olympic_events_table.sql` - Creates the `olympic_events` table.
    3.  `02_create_medal_tables.sql` - Creates individual medal tables for various Olympic Games.
    4.  `03_insert_medal_data_olymic_games.sql` - Populates the `olympic_events` table.
    5.  `04_insert_medal_data_with_id_column.sql` - Populates the individual medal tables with data.

3.  **Database Connection Configuration:**
    The database connection details are configured in `src/main/java/com/duyphuc/olympics/db/DBConnectionManager.java`.
    By default, it is configured as:
    *   **URL:** `jdbc:mysql://localhost:3306/olympicgames`
    *   **User:** `root`
    *   **Password:** `123456`
    If your MySQL setup differs, please update these constants in `DBConnectionManager.java` before running the application.

4.  **Default Admin Credentials:**
    After running the SQL scripts, a default admin user will be created:
    *   **Username:** `admin`
    *   **Password:** `admin` (The hash `240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9` corresponds to "admin")

## 4. How to Run the Program (Using Eclipse IDE)

These steps guide you through setting up and running the project directly from Eclipse IDE.

1.  **Clone the Repository:**
    Open a terminal or Git Bash and run:
    ```bash
    git clone https://github.com/NDuyPhuc/OlympicGames.git
    ```
    Navigate to the directory where you want to store the project before running this command.

2.  **Import Project into Eclipse:**
    *   Open Eclipse IDE.
    *   Select `File` -> `Open Projects from File System...`.
    *   Click the `Directory...` button and navigate to the `OlympicGames` folder you just cloned.
    *   Ensure the project is listed and selected.
    *   Click `Finish`. Eclipse should recognize it as a Maven project.

3.  **Configure VM Arguments for JavaFX (Important for direct Eclipse run):**
    *   On the Eclipse menu bar, select `Run` -> `Run Configurations...`.
    *   In the left panel, find and select `Java Application`. If `MainApp` is already listed under it, select `MainApp`. If not, right-click `Java Application` and select `New Configuration`.
        *   **Name:** `MainApp` (or any descriptive name).
        *   **Project:** Browse and select your `olympics-games` project.
        *   **Main class:** Search and select `com.duyphuc.olympics.MainApp`.
    *   Go to the `Arguments` tab.
    *   In the `VM arguments:` text area, enter the following ( **adjust the path to your JavaFX SDK** ):
        ```
        --module-path "D:\javafx-sdk-21.0.7\lib" --add-modules javafx.controls,javafx.fxml,javafx.swing,javafx.graphics
        ```
        *Replace `"D:\javafx-sdk-21.0.7\lib"` with the actual path to the `lib` folder of your JavaFX SDK installation.*
    *   Click `Apply`. You can close the Run Configurations window for now.

4.  **Maven Clean:**
    *   In the `Package Explorer` view in Eclipse, right-click on the `olympics-games` project.
    *   Select `Run As` -> `Maven clean`.
    *   Wait for the process to complete (check the Console view).

5.  **Maven Install (Builds the project and downloads dependencies):**
    *   Right-click on the `olympics-games` project again.
    *   Select `Run As` -> `Maven install`.
    *   This will download all necessary libraries defined in `pom.xml` and build the project. Wait for completion.

6.  **Update Maven Project (Synchronizes Eclipse with Maven):**
    *   Right-click on the `olympics-games` project.
    *   Select `Maven` -> `Update Project...`.
    *   In the dialog, ensure your project is checked and **tick the box "Force update of Snapshots/Releases"**.
    *   Click `OK`.

7.  **Run the Application:**
    *   In the `Package Explorer`, navigate to `src/main/java` -> `com.duyphuc.olympics`.
    *   Right-click on `MainApp.java`.
    *   Select `Run As` -> `Java Application`.

    The application should now start. If Eclipse still shows compilation errors in the editor but the application runs, these are likely IDE-specific display issues that have been bypassed by the correct Maven build and runtime configuration. The steps above are designed to minimize these IDE display errors.

## 5. Project Structure Overview

```
olympics-games/
├── .metadata/              # Eclipse workspace metadata (if created inside an existing workspace)
├── database_scripts/       # SQL scripts for database setup
│   ├── 00_create_users_table.sql
│   ├── 01_create_olympic_events_table.sql
│   ├── 02_create_medal_tables.sql
│   ├── 03_insert_medal_data_olymic_games.sql
│   └── 04_insert_medal_data_with_id_column.sql
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/duyphuc/olympics/
│   │   │       ├── MainApp.java            # JavaFX Application entry point
│   │   │       ├── Launcher.java           # (Alternative main for JAR, if used)
│   │   │       ├── animation/
│   │   │       ├── controller/
│   │   │       ├── dao/
│   │   │       ├── db/
│   │   │       ├── exception/
│   │   │       ├── model/
│   │   │       ├── service/
│   │   │       └── util/
│   │   └── resources/
│   │       └── com/duyphuc/olympics/
│   │           ├── css/                    # CSS stylesheets
│   │           ├── fxml/                   # FXML view files
│   │           └── images/                 # Image assets
│   └── test/                 # (JUnit tests would go here)
├── target/                 # Build output (compiled classes, JAR file)
├── dependency-reduced-pom.xml # Generated by maven-shade-plugin
└── pom.xml                 # Maven project configuration
```

## 6. Technologies Used

*   **Programming Language:** Java (JDK 20)
*   **Build Tool:** Apache Maven
*   **Database:** MySQL
*   **GUI Framework:** JavaFX (SDK 21.0.7 used for direct Eclipse run configuration)
    *   **UI Libraries:**
        *   MaterialFX
        *   ControlsFX
        *   TilesFX
        *   FontAwesomeFX
        *   Ikonli
*   **Charting Library:** JFreeChart
*   **Password Hashing:** Apache Commons Codec (SHA-256)

## 7. Team Members & Roles

*   **Nguyễn Duy Phúc - 97482403200**: FullStack

## 8. GitHub Repository

The public GitHub repository for this project can be found at:
[https://github.com/NDuyPhuc/OlympicGames](https://github.com/NDuyPhuc/OlympicGames)
