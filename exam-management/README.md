# HỆ THỐNG QUẢN LÝ THI – KHẢO THÍ (SPRING BOOT & REACT & SQL SERVER)

Hệ thống Website hoàn chỉnh Quản lý Thi – Khảo thí được xây dựng bám sát cấu trúc cơ sở dữ liệu **Microsoft SQL Server**, kết hợp **Backend Java Spring Boot RESTful API (Maven)** và **Frontend Web (React / Vite / Tailwind CSS)**. Hệ thống kết nối cơ sở dữ liệu SQL Server thật, thực hiện đầy đủ quy tắc nghiệp vụ: kiểm tra dung lượng phòng thi, tự động sinh số báo danh, phân công cán bộ coi thi, cơ chế Soft Delete & Audit fields, và khóa sửa điểm chặt chẽ từ tầng Backend khi kết quả thi đã chốt.

> **Lưu ý:** `start.bat`, `npm start` và VS Code tasks tự động phát hiện cổng SQL Server chứa database `exam_management`. **Không chạy lại script SQL khi đã có dữ liệu mới**, vì file SQL có chứa `DROP TABLE`.

---

## CẤU TRÚC THƯ MỤC DỰ ÁN

```text
exam-management/
│
├── start.bat                          # File 1-click chạy cả Backend + Frontend cùng lúc
├── package.json                       # Chứa lệnh "npm start" chạy đồng thời cả hai
├── .vscode/tasks.json                 # Cấu hình phím tắt Ctrl+Shift+B trong VS Code
│
├── database/
│   ├── exam_management.sql            # Script T-SQL tạo DB, 12 bảng và Seed data mẫu
│   └── exam_management_export.sql     # Bản export đầy đủ Schema + dữ liệu thực tế từ SQL Server
│
├── backend/
│   ├── pom.xml                        # Cấu hình Maven (Spring Boot 3, Spring Data JPA, mssql-jdbc)
│   ├── start-backend.ps1 / .bat       # Tự động dò cổng SQL Server và chạy Backend
│   └── src/main/
│       ├── java/com/exam/
│       │   ├── ExamManagementApplication.java
│       │   ├── config/                # Cấu hình CORS
│       │   ├── entity/                # Entities cho các bảng nghiệp vụ và danh mục
│       │   ├── repository/            # Spring Data JPA Repositories
│       │   ├── dto/                   # Data Transfer Objects
│       │   ├── service/               # Nghiệp vụ: capacity check, auto SBD, is_locked check, invigilators
│       │   └── controller/            # REST Controllers cho các nghiệp vụ + lookups + student-portal
│       └── resources/
│           └── application.properties # Cấu hình kết nối SQL Server
│
├── frontend/
│   ├── src/
│   │   ├── components/                # Phân trang, Modal, Alert, chọn giám thị, công cụ đăng ký
│   │   ├── screens/                   # 6 màn hình nghiệp vụ + Cổng thông tin sinh viên
│   │   ├── services/                  # Axios API client, in danh sách thi, Google Calendar
│   │   ├── App.jsx                    # Layout Web quản trị responsive
│   │   └── main.jsx
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
│
└── README.md
```

---

## 1. THIẾT KẾ CƠ SỞ DỮ LIỆU SQL SERVER

### A. 8 Bảng nghiệp vụ:
1. `exam_types`: Loại kỳ thi (`id` UNIQUEIDENTIFIER PK, `name`, `description`, và các cột Audit & Soft Delete).
2. `exams`: Lịch thi (`id` PK, `exam_type_id`, `course_class_id`, `semester_id`, `exam_date`, `start_time`, `duration_minutes`, `end_time`, `exam_format`, `exam_status`, `supervisor_count`, và các cột Audit & Soft Delete).
3. `exam_rooms`: Phòng thi (`id` PK, `exam_id`, `room_id`, `capacity`, và các cột Audit & Soft Delete).
4. `exam_registrations`: Danh sách thí sinh (`id` PK, `exam_id`, `exam_room_id`, `student_id`, `roll_number`, và các cột Audit & Soft Delete).
5. `exam_results`: Kết quả thi (`id` PK, `registration_id`, `score`, `status`, `graded_by`, `graded_at`, `is_locked`, `appeal_status`, và các cột Audit & Soft Delete).
6. `exam_papers`: Đề thi (`id` PK, `exam_id`, `paper_code`, `file_url`, và các cột Audit & Soft Delete).
7. `invigilators`: Danh sách cán bộ coi thi (`id`, `staff_code`, `full_name`, `department`, `phone`, `is_active`).
8. `exam_invigilators`: Phân công cán bộ coi thi theo lịch thi (`exam_id`, `invigilator_id`, `assigned_at`).

### B. 4 Bảng phụ phục vụ FOREIGN KEY:
- `courses`: Học phần (`id`, `course_code`, `course_name`, `credits`, `is_active`)
- `semesters`: Học kỳ (`id`, `semester_code`, `semester_name`, `academic_year`, `is_active`)
- `rooms`: Phòng thi vật lý (`id`, `room_number`, `building`, `capacity`, `is_active`)
- `students`: Sinh viên (`id`, `student_code`, `full_name`, `email`, `phone`, `class_name`, `birth_date`, `is_active`)

### C. Quy tắc Soft Delete & Audit Fields:
- Khi tạo: `created_at = SYSDATETIME()`, `created_by = GUID`, `is_active = 1`, `deleted_at = NULL`, `deleted_by = NULL`.
- Khi sửa: `updated_at = SYSDATETIME()`, `updated_by = GUID`.
- Khi xóa (Soft delete): `deleted_at = SYSDATETIME()`, `deleted_by = GUID`, `is_active = 0`.
- Mọi câu truy vấn dữ liệu từ API đều lọc:
  ```sql
  WHERE is_active = 1 AND deleted_at IS NULL
  ```

---

## 2. HƯỚNG DẪN CÀI ĐẶT & CHẠY HỆ THỐNG

### Yêu cầu môi trường
- **Microsoft SQL Server** (2019 / 2022 / 2025 hoặc SQL Server Express)
- **Java 17+** và **Maven 3.8+**
- **Node.js 18+** và **npm**

### BƯỚC 1: KHỞI TẠO DATABASE SQL SERVER
- Mở **SQL Server Management Studio (SSMS)**.
- Mở file `database/exam_management_export.sql` (đầy đủ dữ liệu thực tế) hoặc `database/exam_management.sql` (dữ liệu mẫu gốc).
- Nhấn **Execute (F5)** để tạo database `exam_management`, các bảng, khóa ngoại và dữ liệu.

### BƯỚC 2: CÀI ĐẶT DEPENDENCIES (CHỈ CẦN CHẠY LẦN ĐẦU)
```bash
# Tại thư mục exam-management
npm install
cd frontend
npm install
cd ..
```

### BƯỚC 3: CHẠY CẢ BACKEND + FRONTEND CÙNG LÚC
Chọn 1 trong 3 cách:
1. **Terminal:** chạy `npm start` tại thư mục `exam-management`.
2. **Windows:** nhấp đúp file `start.bat`.
3. **VS Code:** nhấn `Ctrl + Shift + B` → chọn `Chạy Cả Hai (Backend + Frontend)`.

### CHẠY RIÊNG TỪNG PHẦN (NẾU CẦN)
- Kiểm tra cấu hình kết nối tại `backend/src/main/resources/application.properties`:
  ```properties
  server.port=5000
  spring.datasource.url=${EXAM_DB_JDBC_URL:jdbc:sqlserver://127.0.0.1:1433;databaseName=exam_management;encrypt=false;trustServerCertificate=true}
  spring.datasource.username=${EXAM_DB_USER:sa}
  spring.datasource.password=${EXAM_DB_PASSWORD:123456}
  spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
  spring.jpa.hibernate.ddl-auto=none
  ```
- **Backend:**
  ```bash
  cd backend
  mvn spring-boot:run
  ```
  URL API: `http://localhost:5000` · Health Check: `http://localhost:5000/api/health`
- **Frontend:**
  ```bash
  cd frontend
  npm run dev
  ```
  URL Web: `http://localhost:3000`

---

## 3. CÁC TÍNH NĂNG NGHIỆP VỤ TRÊN GIAO DIỆN

1. **Loại kỳ thi**: Danh sách, tìm kiếm, phân trang, thêm mới, sửa, xóa mềm.
2. **Lịch thi**: Danh sách lịch thi, lọc theo học kỳ/trạng thái, phân công cán bộ coi thi, thêm, sửa, xóa mềm.
3. **Phòng thi**: Gán phòng thi cho ca thi, kiểm tra sức chứa thiết kế, kiểm tra trùng ca, theo dõi sĩ số phòng thi.
4. **Danh sách thí sinh**: Đăng ký thí sinh dự thi, tự động kiểm tra sức chứa phòng thi, tự động sinh số báo danh (SBD), in danh sách phòng thi.
5. **Kết quả thi**: Nhập điểm, sửa điểm, chốt điểm (`is_locked = 1` → Backend từ chối sửa với HTTP `403 Forbidden`) và quản lý phúc khảo.
6. **Đề thi**: Quản lý mã đề thi, lưu đường dẫn file đề thi, mở xem tài liệu đề thi trực tiếp.
7. **Cổng sinh viên**: Sinh viên tra cứu lịch thi, phòng thi, số báo danh và kết quả điểm thi.
