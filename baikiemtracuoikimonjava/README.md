# EduManage — Quản lý đào tạo đại học

Ứng dụng Spring Boot quản lý ngành, chương trình đào tạo, môn học, các môn trong từng chương trình và môn tiên quyết. Backend dùng SQL Server có sẵn; giao diện tiếng Việt dùng Thymeleaf, Bootstrap và JavaScript `fetch`.

## 1. Môi trường đã kiểm tra

- Java/Javac 27; Spring Boot 4.1.1 và dự án hiện đã build, chạy thử với phiên bản này.
- Maven Wrapper đã có trong dự án. Không cần cài Maven toàn hệ thống.
- SQL Server instance `localhost\SQLEXPRESS01`, database `quan_ly_dao_tao`.
- TCP/IP SQL Server đang nghe cổng `1433`; Windows Authentication hoạt động trên máy đã kiểm tra.
- Maven tải Microsoft JDBC Driver và thư viện Windows Authentication x64 cho dự án.

Nếu mở dự án trên máy khác, cần cài JDK mà Spring Boot 4.1.1 hỗ trợ, bật TCP/IP cho SQL Server, và cấp quyền database cho tài khoản Windows chạy ứng dụng. Nếu SQL Server dùng cổng khác, sửa `spring.datasource.url` trong `src/main/resources/application.properties`.

## 2. Chạy ứng dụng trong VS Code

Mở Terminal → PowerShell tại thư mục dự án:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Maven Wrapper tự tải Maven và các thư viện cần thiết vào thư mục người dùng. Khi thấy ứng dụng khởi động trên cổng `8080`, mở:

- Website: `http://localhost:8080/dashboard`
- Kiểm tra API và database: `http://localhost:8080/api/v1/health`

Kết quả health thành công:

```json
{
  "status": "UP",
  "database": "quan_ly_dao_tao"
}
```

### Đăng nhập quyền quản trị

Các API `POST`, `PUT`, `DELETE` yêu cầu HTTP Basic Authentication với vai trò `ADMIN`. Người dùng mặc định là `admin`. Nếu không đặt mật khẩu, Spring Security sinh mật khẩu ngẫu nhiên và in trong Terminal khi ứng dụng khởi động; nhập mật khẩu đó tại nút **Đăng nhập quản trị** trên website.

Muốn tự đặt mật khẩu cho lần chạy hiện tại, trước khi khởi động ứng dụng trong cùng Terminal:

```powershell
$env:SPRING_SECURITY_USER_PASSWORD = 'dat-mat-khau-rieng-cua-ban'
.\mvnw.cmd spring-boot:run
```

Không đưa mật khẩu thật vào `application.properties`, Git hoặc Postman collection đã chia sẻ. Website chỉ giữ thông tin Basic Authentication trong `sessionStorage` của tab hiện tại; nút **Đăng xuất** sẽ xóa thông tin này.

## 3. Database — giữ nguyên schema hiện có

Ứng dụng chỉ dùng database `quan_ly_dao_tao`. Hibernate chạy với:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.sql.init.mode=never
```

Ứng dụng không tạo hoặc sửa bảng. Entity ánh xạ theo schema `dbo` đã đọc từ SQL Server:

| Bảng | Các cột đã kiểm tra |
|---|---|
| `majors` | `id BIGINT IDENTITY`, `major_code VARCHAR(20)`, `major_name NVARCHAR(150)`, `description NVARCHAR(MAX)`, `is_active BIT` |
| `training_programs` | `id BIGINT IDENTITY`, `program_code VARCHAR(30)`, `program_name NVARCHAR(200)`, `major_id BIGINT`, `cohort VARCHAR(20)`, `academic_year VARCHAR(20)`, `total_credits INT`, `description NVARCHAR(MAX)`, `is_active BIT` |
| `courses` | `id BIGINT IDENTITY`, `course_code VARCHAR(20)`, `course_name NVARCHAR(200)`, `credits INT`, `theory_credits INT`, `practice_credits INT`, `description NVARCHAR(MAX)`, `is_active BIT` |
| `program_courses` | `id BIGINT IDENTITY`, `program_id BIGINT`, `course_id BIGINT`, `semester_index INT`, `is_compulsory BIT` |
| `course_prerequisites` | `id BIGINT IDENTITY`, `course_id BIGINT`, `prerequisite_course_id BIGINT`, `type VARCHAR(30)` |

Các khóa ngoại, mã duy nhất và CHECK constraint hiện có đã được giữ nguyên. Database đang có dữ liệu mẫu. Nút xóa trên giao diện chỉ đổi `is_active` thành `false`; không xóa cứng dữ liệu.

SQL Server hiện dùng chứng chỉ TLS chưa được máy tin cậy, vì vậy cấu hình kết nối cục bộ có `trustServerCertificate=true`. Đây là cấu hình phát triển trên máy cá nhân; môi trường triển khai thật cần cấu hình chứng chỉ SQL Server được tin cậy.

**Giới hạn từ schema thực tế:** bảng `courses` không có cột loại môn. Do đó `GET /api/v1/metadata` trả `courseTypes: []` và có ghi chú; không tự ý thêm cột. Bắt buộc/tự chọn được lưu đúng tại `program_courses.is_compulsory`. Loại tiên quyết được lấy từ bảng hiện có (hiện có `REQUIRED`).

## 4. API REST

Base URL: `http://localhost:8080/api/v1`. Các danh sách trả `content`, `page` (bắt đầu từ 0), `limit`, `totalElements`, `totalPages`, `first`, `last`. Tham số `limit` tối đa là 100.

| Nhóm | Endpoint |
|---|---|
| Health/metadata | `GET /health`, `GET /metadata` |
| Ngành | `GET /majors?q=&page=0&limit=10`, `GET /majors/{id}`, `POST /majors`, `PUT /majors/{id}`, `DELETE /majors/{id}` |
| Môn học | `GET /courses?q=&credits=&page=0&limit=10`, `GET /courses/{id}`, `POST /courses`, `PUT /courses/{id}`, `DELETE /courses/{id}` |
| Chương trình | `GET /training-programs?majorId=&cohort=&academicYear=&q=&page=0&limit=10`, `GET /training-programs/{id}`, `POST /training-programs`, `PUT /training-programs/{id}`, `DELETE /training-programs/{id}` |
| Môn trong chương trình | `POST /training-programs/{id}/courses`, `DELETE /training-programs/{id}/courses/{courseId}` |
| Môn tiên quyết | `GET /courses/{id}/prerequisites`, `POST /courses/{id}/prerequisites`, `DELETE /courses/{id}/prerequisites/{prerequisiteId}` |

Ví dụ body:

```json
{
  "majorCode": "KTPM",
  "majorName": "Kỹ thuật phần mềm",
  "description": "Chương trình đào tạo kỹ thuật phần mềm"
}
```

```json
{
  "courseCode": "JAVA101",
  "courseName": "Lập trình Java",
  "credits": 3,
  "theoryCredits": 2,
  "practiceCredits": 1,
  "description": "Nhập môn Java"
}
```

```json
{
  "programCode": "KTPM2026",
  "programName": "Chương trình Kỹ thuật phần mềm",
  "majorId": 1,
  "cohort": "2026",
  "academicYear": "2026-2030"
}
```

```json
{
  "courseId": 1,
  "semesterIndex": 1,
  "isCompulsory": true
}
```

```json
{
  "prerequisiteCourseId": 1,
  "type": "REQUIRED"
}
```

Trong Postman, chọn **Authorization → Basic Auth**, nhập `admin` và mật khẩu ở Terminal. Thử lỗi an toàn bằng cách gửi body thiếu mã/tên (400), một mã đã tồn tại (409), ID rất lớn không có trong bảng (404), môn tự làm tiên quyết (400), hoặc tạo `CS101 → CS102` khi dữ liệu hiện tại đã có `CS102 → CS101` (409). Không thử DELETE trên dữ liệu mẫu nếu không muốn ngừng sử dụng bản ghi đó.

Các mã lỗi validation và xung đột trả JSON có `timestamp`, `status`, `error`, `message`, `path`; lỗi máy chủ không trả chi tiết nội bộ.

## 5. Cấu trúc source

```text
src/main/java/vn/edu/quanlydaotao/
├── config/       Cấu hình Spring Security
├── controller/   REST API và trang Thymeleaf
├── dto/          Request/response API
├── entity/       Ánh xạ các bảng SQL Server
├── exception/    Lỗi API và xử lý tập trung
├── repository/   Spring Data JPA
└── service/      Nghiệp vụ

src/main/resources/
├── static/css/   Giao diện
├── static/js/    Fetch API và tương tác
├── templates/    Các trang Thymeleaf
└── application.properties
```
