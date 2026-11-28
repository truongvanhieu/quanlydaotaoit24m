> **Bản sửa kết nối SQL Server:** Xem `HUONG_DAN_SUA_LOI_SQL_SERVER.txt` trước khi chạy. `start.bat`, `npm run dev` và VS Code tasks hiện tự phát hiện cổng SQL Server chứa database `exam_management`. **Không chạy lại script SQL khi đã có dữ liệu**, vì file SQL gốc chứa `DROP TABLE`.

# HỆ THỐNG QUẢN LÝ THI – KHẢO THÍ (SPRING BOOT & REACT & SQL SERVER)

Hệ thống Website hoàn chỉnh Quản lý Thi – Khảo thí được xây dựng bám sát 100% chuẩn cấu trúc cơ sở dữ liệu **Microsoft SQL Server**, kết hợp **Backend Java Spring Boot RESTful API (Maven)** và **Frontend Web (React/Tailwind CSS)**. Hệ thống kết nối cơ sở dữ liệu SQL Server thật, không sử dụng mockup hay dữ liệu hardcode, thực hiện đầy đủ quy tắc nghiệp vụ, kiểm tra ràng buộc dung lượng phòng thi, tự động sinh số báo danh, cơ chế Soft Delete & Audit fields, và khóa sửa điểm chặt chẽ từ tầng Backend khi kết quả thi đã chốt.

---

## CẤU TRÚC THƯ MỤC DỰ ÁN

```text
exam-management/
│
├── start.bat                 # File 1-click chạy cả Backend + Frontend cùng lúc
├── package.json              # Chứa lệnh "npm start" chạy đồng thời cả hai
├── .vscode/tasks.json        # Cấu hình phím tắt Ctrl+Shift+B trong VS Code
│
├── database/
│   └── exam_management.sql   # Script T-SQL tạo DB, 6 bảng chính, 4 bảng phụ FK, PK, Seed data
│
├── backend/
│   ├── pom.xml                   # Cấu hình Maven (Spring Boot 3, Spring Data JPA, mssql-jdbc)
│   └── src/
│       └── main/
│           ├── java/com/exam/
│           │   ├── ExamManagementApplication.java
│           │   ├── config/       # Cấu hình CORS
│           │   ├── entity/       # 6 Entity chính + 4 Entity phụ
│           │   ├── repository/   # Spring Data JPA Repositories
│           │   ├── dto/          # Data Transfer Objects
│           │   ├── service/      # Nghiệp vụ: capacity check, auto SBD, is_locked check
│           │   └── controller/   # REST Controllers cho 6 nghiệp vụ + lookups + student-portal
│           └── resources/
│               └── application.properties # Cấu hình kết nối SQL Server
│
├── frontend/
│   ├── src/
│   │   ├── components/           # Phân trang, Hộp thoại (Modal), Thông báo (Alert)
│   │   ├── screens/              # 6 màn hình nghiệp vụ + Cổng thông tin sinh viên
│   │   ├── services/api.js       # Axios Client gọi Spring Boot API
│   │   ├── App.jsx               # Layout Web quản trị responsive, full-width
│   │   └── main.jsx
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
│
└── README.md
```

---

## 1. THIẾT KẾ CƠ SỞ DỮ LIỆU SQL SERVER

### A. 6 Bảng nghiệp vụ cốt lõi:
1. `exam_types`: Loại kỳ thi (`id` UNIQUEIDENTIFIER PK, `name`, `description`, và các cột Audit & Soft Delete).
2. `exams`: Lịch thi (`id` PK, `exam_type_id`, `course_class_id`, `semester_id`, `exam_date`, `start_time`, `duration_minutes`, `end_time`, `exam_format`, `exam_status`, `supervisor_count`, và các cột Audit & Soft Delete).
3. `exam_rooms`: Phòng thi (`id` PK, `exam_id`, `room_id`, `capacity`, và các cột Audit & Soft Delete).
4. `exam_registrations`: Danh sách thí sinh (`id` PK, `exam_id`, `exam_room_id`, `student_id`, `roll_number`, và các cột Audit & Soft Delete).
5. `exam_results`: Kết quả thi (`id` PK, `registration_id`, `score`, `status`, `graded_by`, `graded_at`, `is_locked`, `appeal_status`, và các cột Audit & Soft Delete).
6. `exam_papers`: Đề thi (`id` PK, `exam_id`, `paper_code`, `file_url`, và các cột Audit & Soft Delete).

### B. 4 Bảng phụ tối giản phục vụ FOREIGN KEY:
- `courses`: Học phần (`id`, `course_code`, `course_name`, `credits`, `is_active`)
- `semesters`: Học kỳ (`id`, `semester_code`, `semester_name`, `academic_year`, `is_active`)
- `rooms`: Phòng thi vật lý (`id`, `room_number`, `building`, `capacity`, `is_active`)
- `students`: Sinh viên (`id`, `student_code`, `full_name`, `email`, `phone`, `class_name`, `is_active`)

### C. Quy tắc Soft Delete & Audit Fields:
- Khi tạo: `created_at = SYSDATETIME()`, `created_by = GUID`, `is_active = 1`, `deleted_at = NULL`, `deleted_by = NULL`.
- Khi sửa: `updated_at = SYSDATETIME()`, `updated_by = GUID`.
- Khi xóa (Soft delete): `deleted_at = SYSDATETIME()`, `deleted_by = GUID`, `is_active = 0`.
- Mọi câu truy vấn dữ liệu từ API đều có bộ lọc:
  ```sql
  WHERE is_active = 1 AND deleted_at IS NULL
  ```

---

## 2. HƯỚNG DẪN CÀI ĐẶT & CHẠY HỆ THỐNG

### Thứ tự thực hiện:
```text
1. Khởi động SQL Server
       ↓
2. Chạy database/exam_management.sql
       ↓
3. Khởi động Backend Spring Boot
       ↓
4. Khởi động Frontend Web
```

---

### BƯỚC 1: KHỞI TẠO DATABASE SQL SERVER
- Mở **SQL Server Management Studio (SSMS)**.
- Mở file: `database/exam_management.sql`.
- Nhấn **Execute (F5)** để tạo database `exam_management`, các bảng, ràng buộc khóa chính/khóa ngoại và nạp dữ liệu mẫu (Seed Data).
- Tạo tài khoản SQL Server Authentication nếu cần:
  ```sql
  -- Dang nhap bang sa da duoc cai dat va bat SQL Server Authentication; khong CREATE LOGIN sa.
  USE exam_management;
  -- Tai khoan sa da co san, khong can CREATE USER sa.
  -- Tai khoan sa la sysadmin; khong can them vao db_owner.
  ```

---

## ⚡ CÁCH CHẠY CẢ 2 (BACKEND + FRONTEND) CÙNG MỘT LÚC

Bạn có thể chạy cả 2 hệ thống đồng thời chỉ bằng **1 thao tác** qua bất kỳ cách nào dưới đây:

### Cách 1: Gõ 1 lệnh duy nhất trong VS Code Terminal
Mở terminal tại thư mục gốc `exam-management` và gõ:
```bash
npm start
```
*(Hệ thống sử dụng `concurrently` để chạy song song Spring Boot Backend và React Frontend trong cùng một cửa sổ, phân biệt màu xanh/tím rất trực quan).*

### Cách 2: Bấm đúp chuột vào file `start.bat`
Chỉ cần nhấp đúp file **`start.bat`** (hoặc gõ `.\start.bat` trong terminal), Windows sẽ tự động mở 2 cửa sổ cho Backend và Frontend.

### Cách 3: Phím tắt trong VS Code
Trong VS Code, bấm tổ hợp phím **`Ctrl + Shift + B`** (hoặc chọn menu **Terminal** → **Run Build Task...**), chọn **`Chạy Cả Hai (Backend + Frontend)`**.

---

### BƯỚC 2: CẤU HÌNH & CHẠY RIÊNG TỪNG PHẦN (NẾU CẦN)
- **Yêu cầu môi trường**: Java 17+, Maven 3.8+.
- Mở file `backend/src/main/resources/application.properties` để kiểm tra kết nối:
  ```properties
  server.port=5000
  spring.datasource.url=jdbc:sqlserver://127.0.0.1:1433;databaseName=exam_management;encrypt=false;trustServerCertificate=true
  spring.datasource.username=sa
  spring.datasource.password=123123
  spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
  spring.jpa.hibernate.ddl-auto=none
  ```
  *(Lưu ý: Nếu SQL Server chạy port mặc định 1433 thì đổi port thành `1433`)*.

- Chạy Backend bằng Maven:
  ```bash
  cd backend
  mvn spring-boot:run
  ```
  hoặc chạy file `target/exam-management-backend-1.0.0.jar`:
  ```bash
  java -jar target/exam-management-backend-1.0.0.jar
  ```
  hoặc chạy trực tiếp class `ExamManagementApplication.java` trong VS Code/IntelliJ IDEA.

- **URL Backend API**: `http://localhost:5000`
- **Health Check**: `http://localhost:5000/api/health`

---

### BƯỚC 3: CÀI ĐẶT & CHẠY FRONTEND WEB
- Di chuyển vào thư mục `frontend`:
  ```bash
  cd frontend
  npm install
  npm run dev
  ```
- Mở trình duyệt Web tại:
  ```text
  http://localhost:3000   (hoặc http://localhost:3001)
  ```

---

## 3. CÁC TÍNH NĂNG NGHIỆP VỤ TRÊN GIAO DIỆN

Giao diện được thiết kế chuẩn Web ứng dụng quản trị (`width: 100%`, responsive, thanh menu bên trái + vùng nội dung bảng rộng rãi):

1. **Loại kỳ thi**: Danh sách, tìm kiếm, phân trang, thêm mới, sửa, xóa mềm.
2. **Lịch thi**: Danh sách lịch thi, tìm kiếm, phân trang, lọc theo học kỳ, lọc trạng thái, thêm, sửa, xóa mềm.
3. **Phòng thi**: Gán phòng thi cho ca thi, kiểm tra sức chứa thiết kế, kiểm tra trùng ca, theo dõi sĩ số phòng thi bằng thanh tiến độ trực quan.
4. **Danh sách thí sinh**: Đăng ký thí sinh dự thi, tự động kiểm tra sức chứa phòng thi (chặn đăng ký nếu phòng đã đầy), tự động sinh số báo danh (SBD).
5. **Kết quả thi**: Nhập điểm, sửa điểm, chốt điểm (khóa điểm) và quản lý phúc khảo.
   * **Bảo mật khóa điểm**: Khi kết quả thi đã chốt (`is_locked = 1`), Frontend khóa form không cho sửa và **Backend Spring Boot bắt buộc từ chối chỉnh sửa với mã lỗi HTTP 403 Forbidden**.
6. **Đề thi**: Quản lý mã đề thi, lưu đường dẫn file đề thi, mở xem tài liệu đề thi trực tiếp.
7. **Cổng sinh viên**: Sinh viên tra cứu lịch thi, phòng thi, số báo danh và kết quả điểm thi chính thức của mình.
