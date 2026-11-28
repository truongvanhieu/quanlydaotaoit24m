# Nhóm 10 – Hệ thống Quản lý Thi & Khảo thí (Exam Management System)

Dự án thuộc môn **Quản lý Đào tạo (`quanlydaotaoit24m` – nhánh `nhom-10`)**, phụ trách phân hệ **X. Quản lý Thi – Khảo thí**.

Hệ thống được xây dựng theo kiến trúc **Spring Boot RESTful API (Java / Maven)** + **ReactJS (Vite / Tailwind CSS)** + **Microsoft SQL Server**, hỗ trợ đầy đủ nghiệp vụ quản lý thi, kiểm tra dung lượng phòng thi, tự động sinh số báo danh, phân công cán bộ coi thi, chốt/khóa điểm từ Backend, phúc khảo và cổng tra cứu sinh viên.

---

## Cấu trúc thư mục

```text
quanlydaotaoit24m/ (nhánh nhom-10)
├── database/
│   ├── exam_management_export.sql    # Database export đầy đủ (Schema + dữ liệu thực tế 12 bảng)
│   └── it24m.sql                     # Database mẫu của hệ thống đào tạo chung
└── exam-management/                  # Source code chính của Nhóm 10
    ├── database/
    │   ├── exam_management.sql           # Script khởi tạo DB + dữ liệu mẫu (Seed data)
    │   └── exam_management_export.sql    # Bản export đầy đủ từ SQL Server
    ├── backend/                      # Spring Boot 3 API (port 5000)
    ├── frontend/                     # React + Vite Web App (port 3000)
    ├── package.json                  # Script chạy song song Backend & Frontend
    ├── start.bat                     # Khởi chạy nhanh trên Windows
    └── README.md                     # Tài liệu kỹ thuật chi tiết
```

---

## Cơ sở dữ liệu (`exam_management` – SQL Server)

Gồm **12 bảng** (8 bảng nghiệp vụ + 4 bảng danh mục khóa ngoại):

| Nhóm | Bảng | Mô tả |
|---|---|---|
| Nghiệp vụ chính | `exam_types` | Loại kỳ thi (Giữa kỳ, Cuối kỳ, Thi lại / Cải thiện...) |
| Nghiệp vụ chính | `exams` | Lịch thi (ngày thi, giờ bắt đầu/kết thúc, hình thức, trạng thái) |
| Nghiệp vụ chính | `exam_rooms` | Phân phòng thi cho từng ca thi & kiểm soát sức chứa |
| Nghiệp vụ chính | `exam_registrations` | Danh sách thí sinh dự thi & số báo danh (`roll_number`) |
| Nghiệp vụ chính | `exam_results` | Kết quả thi, trạng thái chốt điểm (`is_locked`), phúc khảo (`appeal_status`) |
| Nghiệp vụ chính | `exam_papers` | Quản lý mã đề thi & đường dẫn tệp đề thi |
| Nghiệp vụ chính | `invigilators`, `exam_invigilators` | Quản lý & phân công cán bộ coi thi |
| Danh mục (FK) | `courses`, `semesters`, `rooms`, `students` | Học phần, Học kỳ, Phòng học vật lý, Sinh viên |

---

## Hướng dẫn cài đặt & chạy nhanh

### Yêu cầu môi trường
- **Microsoft SQL Server** (2019 / 2022 / 2025 hoặc SQL Server Express)
- **Java 17+** & **Maven 3.8+**
- **Node.js 18+** & **npm**

### Bước 1: Import Database
1. Mở **SQL Server Management Studio (SSMS)**.
2. Mở file `database/exam_management_export.sql` (chứa đầy đủ schema + dữ liệu thực tế) hoặc `exam-management/database/exam_management.sql`.
3. Nhấn **Execute (F5)** để tạo database `exam_management`.

> **Lưu ý:** File SQL có lệnh `DROP TABLE` để tạo lại bảng sạch — không chạy đè nếu bạn đang muốn giữ dữ liệu mới nhập thêm.

### Bước 2: Cấu hình kết nối SQL Server (nếu cần)
Kiểm tra file `exam-management/backend/src/main/resources/application.properties`:
```properties
server.port=5000
spring.datasource.url=${EXAM_DB_JDBC_URL:jdbc:sqlserver://127.0.0.1:1433;databaseName=exam_management;encrypt=false;trustServerCertificate=true}
spring.datasource.username=${EXAM_DB_USER:sa}
spring.datasource.password=${EXAM_DB_PASSWORD:123456}
```
*(Có thể ghi đè qua biến môi trường `EXAM_DB_JDBC_URL`, `EXAM_DB_USER`, `EXAM_DB_PASSWORD`; script `start-backend.ps1` tự động dò cổng TCP của SQL Server trên Windows).*

### Bước 3: Cài dependencies & khởi chạy
```bash
# Cài đặt packages cho root và frontend
cd exam-management
npm install
cd frontend
npm install
cd ..

# Chạy đồng thời Backend (http://localhost:5000) và Frontend (http://localhost:3000)
npm start
```
Hoặc chạy riêng từng phần:
- **Backend:** `cd exam-management/backend && mvn spring-boot:run` → `http://localhost:5000/api/health`
- **Frontend:** `cd exam-management/frontend && npm run dev` → `http://localhost:3000`

---

## Tính năng chính
1. **Quản lý Loại kỳ thi** – Thêm, sửa, xóa mềm, tìm kiếm, phân trang.
2. **Quản lý Lịch thi** – Lọc theo học kỳ/trạng thái, kiểm tra thời gian thi, phân công cán bộ coi thi.
3. **Phân phòng thi** – Gán phòng thi theo ca, kiểm tra trùng ca & sức chứa phòng.
4. **Danh sách thí sinh** – Đăng ký thi, chặn vượt sức chứa phòng, tự động sinh số báo danh (SBD), hỗ trợ in danh sách thi.
5. **Kết quả thi & Phúc khảo** – Nhập/sửa điểm, chốt khóa điểm (`is_locked = 1` → Backend chặn sửa với HTTP `403 Forbidden`), theo dõi trạng thái phúc khảo.
6. **Quản lý Đề thi** – Lưu mã đề và đường dẫn tệp đề thi theo từng kỳ thi.
7. **Cổng sinh viên** – Sinh viên tra cứu lịch thi, phòng thi, số báo danh và điểm thi.

Xem thêm chi tiết kỹ thuật tại [`exam-management/README.md`](exam-management/README.md).
