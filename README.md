![edit_icon](https://github.com/user-attachments/assets/d3d69b8d-ae92-44d2-8ab2-5a489ef42eb1)# OlympicGames


```
OlympicGamesMedalAnalyzer/
├── pom.xml                     // File cấu hình Maven (quản lý thư viện)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── duyphuc/
│   │   │           └── olympics/
│   │   │               ├── MainApp.java        // Lớp chính khởi chạy ứng dụng JavaFX
│   │   │               │
│   │   │               ├── controller/         // Chứa các lớp JavaFX Controller
│   │   │               │   ├── LoginController.java
│   │   │               │   ├── MainDashboardController.java
│   │   │               │   ├── MedalManagementController.java
│   │   │               │   ├── UserProfileController.java
│   │   │               │   ├── AdminUserManagementController.java
│   │   │               │   ├── ReportController.java 
│   │   │               │   └── ChartViewController.java
│   │   │               │
│   │   │               │── animation/
│   │   │               │   ├── OlympicRingsAnimation.java
│   │   │               │   └── ParticleSystem.java 
│   │   │               │
│   │   │               ├── model/              // Chứa các lớp thực thể (POJO)
│   │   │               │   ├── User.java
│   │   │               │   ├── MedalEntry.java     // Đại diện cho một dòng trong bảng huy chương
│   │   │               │   └── OlympicEvent.java   // Đại diện cho một kỳ Olympic (tên, năm, tên bảng DB)
│   │   │               │
│   │   │               ├── dao/                // Data Access Objects - Tương tác với CSDL
│   │   │               │   ├── UserDAO.java
│   │   │               │   ├── MedalDAO.java
│   │   │               │   └── OlympicEventDAO.java // Để lấy danh sách các kỳ Olympic
│   │   │               │
│   │   │               ├── service/            // Logic nghiệp vụ
│   │   │               │   ├── AuthService.java    // Xử lý đăng nhập, session
│   │   │               │   ├── MedalService.java   // Xử lý logic dữ liệu huy chương, tính toán
│   │   │               │   ├── ChartService.java   // Tạo và chuẩn bị dữ liệu cho JFreeChart
│   │   │               │   ├── ReportService.java   // Tạo và chuẩn bị dữ liệu cho JFreeChart
│   │   │               │   └── ReportService.java  // (Extra) Tạo các báo cáo, derivable data
│   │   │               │
│   │   │               ├── db/                 // Quản lý kết nối CSDL
│   │   │               │   └── DBConnectionManager.java // Singleton pattern
│   │   │               │
│   │   │               ├── util/               // Các lớp tiện ích
│   │   │               │   ├── PasswordHasher.java
│   │   │               │   ├── FxmlLoaderUtil.java // Tiện ích tải FXML
│   │   │               │   └── AlertUtil.java      // Hiển thị dialog thông báo
│   │   │               │
│   │   │               └── exception/          // Các lớp Exception tùy chỉnh (nếu cần)
│   │   │                   ├── AuthenticationException.java
│   │   │                   ├── DataAccessException.java
│   │   │                   └── InvalidInputException.java
│   │   │
│   │   └── resources/
│   │       └── com/
│   │           └── duyphuc/
│   │               └── olympics/
│   │                   ├── fxml/               // Chứa các file FXML cho giao diện
│   │                   │   ├── LoginView.fxml
│   │                   │   ├── MainDashboardView.fxml
│   │                   │   ├── MedalManagementView.fxml
│   │                   │   ├── ReportView.fxml
│   │                   │   ├── UserProfileView.fxml
│   │                   │   ├── UserFormDialog.fxml
│   │                   │   ├── AdminUserManagementView.fxml 
│   │                   │   └── ChartView.fxml
│   │                   │
│   │                   ├── css/                // (Optional) Chứa file CSS cho giao diện
│   │                   │   ├── AdminUserManagementView.css
│   │                   │   ├── login.css
│   │                   │   ├── ChartView.css
│   │                   │   ├── dashboard_styles.css
│   │                   │   ├── medal_management_styles.css
│   │                   │   ├── ReportView.css
│   │                   │   └── UserProfileStyles.css
│   │                   │
│   │                   └── images/             // (Optional) Chứa hình ảnh
│   │                      ├── add_user_icon.png
│   │                      ├── delete_icon.png
│   │                      ├── edit_icon.png
│   │                      ├── Olympic_rings.png
│   │                      ├── refresh_icon.png
│   │                      └── users_icon.png  
│   │
│   └── test/                   // (Optional but recommended) Chứa các lớp test
│       └── java/
│           └── com/
│               └── duyphuc/
│                   └── olympics/
│                       ├── service/
│                       │   └── AuthServiceTest.java
│                       └── dao/
│                           └── MedalDAOTest.java
│
├── database_scripts/           // Chứa các script SQL
│   ├── 00_create_users_table.sql
│   ├── 01_create_olympic_events_table.sql
│   ├── 02_create_medal_tables.sql // Script tạo các bảng huy chương đã cho
│   ├── 03_insert_olympic_events_data.sql
│   └── 04_insert_medal_data.sql   // Script insert dữ liệu huy chương đã cho
│
├── reports/                    // (Generated) Nơi lưu các báo cáo, biểu đồ xuất ra
│
├── README.md                   // Hướng dẫn cài đặt, chạy dự án, mô tả
└── OlympicGamesMedalAnalyzer.docx // Báo cáo dự án
```


