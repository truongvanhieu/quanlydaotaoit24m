================================================================================
HỆ THỐNG QUẢN LÝ ĐÀO TẠO — PHÂN HỆ NHÓM 5 (LỚP IT24M)
QUẢN LÝ NĂM HỌC, HỌC KỲ, LỚP HỌC PHẦN, ĐĂNG KÝ HỌC PHẦN & PHÂN CÔNG GIẢNG VIÊN
================================================================================

1. THÔNG TIN NHÓM THỰC HIỆN
--------------------------------------------------------------------------------
- Trường       : Trường Đại học Đông Á — Khoa Công nghệ Thông tin
- Học phần     : Lập trình Web 1
- Giảng viên HD: ThS. Trương Văn Hiệu
- Lớp          : IT24M — Nhóm 5
- Thành viên   :
    1. Trần Quốc Hưng (Nhóm trưởng)
    2. Nguyễn Anh Khoa
    3. Trịnh Thanh Học

2. LIÊN KẾT MÃ NGUỒN GITHUB
--------------------------------------------------------------------------------
- Repository Nhóm 5 (Fork/Branch n5): https://github.com/Hoc04/quanlydaotaoit24m (nhánh n5 / main)
- Repository chung của lớp IT24M    : https://github.com/truongvanhieu/quanlydaotaoit24m
- Pull Request tích hợp Nhóm 5      : https://github.com/truongvanhieu/quanlydaotaoit24m/pull/6

3. YÊU CẦU MÔI TRƯỜNG CÀI ĐẶT
--------------------------------------------------------------------------------
- Java Development Kit (JDK) : Java 21 (khuyến nghị Eclipse Temurin / Oracle JDK 21)
- Build Tool                 : Apache Maven 3.9+ (hoặc dùng sẵn Maven Wrapper: ./mvnw / mvnw.cmd)
- Cơ sở dữ liệu              : Microsoft SQL Server 2019 / 2022 + SSMS
- Trình duyệt                : Chrome / Edge (hỗ trợ Desktop & Mobile Responsive)

4. CẤU HÌNH & PHỤC HỒI CƠ SỞ DỮ LIỆU SQL SERVER
--------------------------------------------------------------------------------
- File backup SQL Server đi kèm: database/it24m.sql (đầy đủ cấu trúc 6 bảng Nhóm 5 & dữ liệu mẫu)
- Thông tin tài khoản SQL Server mặc định trong src/main/resources/application.properties:
    + Host / Port   : localhost:1433
    + Database Name : it24m (tương thích hoàn toàn với QuanLyDaoTao)
    + Username      : sa
    + Password      : 123123
    + JDBC URL      : jdbc:sqlserver://localhost:1433;databaseName=it24m;encrypt=true;trustServerCertificate=true

- Các bước khôi phục CSDL trên SQL Server Management Studio (SSMS):
    Bước 1: Mở SSMS, đăng nhập tài khoản sa (mật khẩu 123123) hoặc Windows Authentication.
    Bước 2: Tạo database mới tên [it24m]:
            CREATE DATABASE it24m;
    Bước 3: Mở file database/it24m.sql (hoặc chạy lệnh sqlcmd) và nhấn Execute (F5) để nạp toàn bộ bảng và dữ liệu mẫu.
    Bước 4: (Tùy chọn) Nếu máy dùng mật khẩu/tên DB khác, cập nhật tại:
            src/main/resources/application.properties
            hoặc truyền biến môi trường DB_URL, DB_USERNAME, DB_PASSWORD.

5. HƯỚNG DẪN CHẠY DỰ ÁN
--------------------------------------------------------------------------------
Bước 1: Giải nén file mã nguồn (Nhom5_QuanLyDaoTao_IT24M_SourceCode.zip) hoặc clone từ GitHub:
        git clone -b n5 https://github.com/Hoc04/quanlydaotaoit24m.git
        cd quanlydaotaoit24m

Bước 2: Chạy ứng dụng Spring Boot bằng Maven Wrapper (không cần cài Maven rời):
        - Trên Windows (PowerShell / CMD):
          .\mvnw.cmd spring-boot:run
        - Trên Linux / macOS:
          ./mvnw spring-boot:run

Bước 3: Chạy bộ kiểm thử tự động JUnit 5 (tùy chọn):
        .\mvnw.cmd test

Bước 4: Mở trình duyệt truy cập hệ thống:
        - Giao diện chính Phân hệ Nhóm 5 : http://localhost:8080/n5.html
        - Trang chủ tích hợp toàn dự án  : http://localhost:8080/index.html

6. DANH SÁCH 6 BẢNG CSDL & 5 NGHIỆP VỤ CỐT LÕI NHÓM 5
--------------------------------------------------------------------------------
- 6 bảng dữ liệu chuẩn Google Sheets N5:
    1. academic_years           (Niên khóa đào tạo — 14 cột)
    2. school_years             (Năm học — 14 cột)
    3. semesters                (Học kỳ — 14 cột)
    4. course_sections          (Lớp học phần — 21 cột)
    5. student_course_sections  (Đăng ký học phần — 15 cột)
    6. lecturer_course_classes  (Phân công giảng viên — 11 cột)

- 5 Tab nghiệp vụ trên http://localhost:8080/n5.html:
    1. Quản lý Năm học (CRUD, lọc trạng thái, kiểm tra start_date < end_date, Xóa mềm)
    2. Quản lý Học kỳ (gắn với Năm học cha, lọc theo Năm học, Xóa mềm)
    3. Quản lý Lớp học phần (nhãn đầy đủ [Mã HP] - Tên HP - Nhóm (HK), kiểm tra sĩ số min/max)
    4. Quản lý Đăng ký học phần & Tra cứu sinh viên theo Lớp học phần (chặn trùng ĐK, chặn vượt max_students, thống kê SV Đang học / Tổng ĐK)
    5. Quản lý Phân công giảng viên (Giảng viên chính / Trợ giảng, chống phân công trùng)

7. DANH MỤC TỆP NỘP TRONG THƯ MỤC BÁO CÁO NHÓM 5
--------------------------------------------------------------------------------
1. BaoCao_KetThucHocPhan_HeThongQuanLyDaoTao_Nhom5_Chuan.docx : File Word báo cáo hoàn chỉnh (53 trang)
2. Nhom5_QuanLyDaoTao_IT24M_SourceCode.zip                    : Toàn bộ mã nguồn Backend/Frontend + database/it24m.sql
3. it24m.sql                                                  : File Backup SQL Server (cấu trúc + dữ liệu mẫu)
4. ReadMe.txt                                                 : File hướng dẫn cài đặt, tài khoản SQL Server & chạy dự án
================================================================================
