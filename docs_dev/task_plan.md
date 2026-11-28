# Task Plan: Hoàn thiện Web Module N5 Quản Lý Đào Tạo IT24M

## 5 Bot Roles Execution Summary
1. **Bot 1 (Backend & Database Architecture)**: 
   - Rà soát & chuẩn hóa toàn bộ Entity: `BaseEntity`, `AcademicYear`, `SchoolYear`, `Semester`, `CourseSection`, `StudentCourseSection`, `LecturerCourseClass`, `Student`.
   - Đáp ứng 100% tài liệu phân tích bảng dữ liệu `Phân_tích_các_bảng_dữ_liệu_Quanlydaotao_IT24M.xlsx.ods` cho Nhóm 5.
   - Thêm đầy đủ audit fields (`created_at`, `updated_at`, `created_by`, `updated_by`, `deleted_at`, `deleted_by`, `is_active`).

2. **Bot 2 (Logic & API Validation)**: 
   - Đảm bảo toàn bộ CRUD, tìm kiếm, lọc theo trạng thái cho 6 bảng của N5.
   - Ràng buộc nghiệp vụ: chống trùng lặp đăng ký học phần, kiểm tra trạng thái lớp đang mở, kiểm tra giới hạn sĩ số tối đa của lớp học phần.
   - Tự động map và đồng bộ thông tin sinh viên (`studentCode`, `studentName`) và lớp học phần (`courseClassName`).
   - Xử lý ngoại lệ chuẩn JSON qua `GlobalExceptionHandler`.

3. **Bot 3 (Frontend Web UI/UX)**: 
   - Xây dựng giao diện web mới trên `index.html` với đầy đủ 6 tabs chức năng của Nhóm 5 + 1 tab Sinh viên.
   - 6 thẻ Stat Cards thống kê số lượng tổng và số lượng đang hoạt động theo thời gian thực với gradient màu hiện đại.
   - Bảng dữ liệu có badge trạng thái trực quan, ô tìm kiếm nhanh và bộ lọc theo trạng thái (Tất cả, Đang hoạt động, Đã khóa).
   - Modal Form Thêm/Sửa chuyên biệt cho từng bảng với dropdown chọn liên kết (Năm học -> Học kỳ -> Lớp học phần -> Sinh viên), không bắt người dùng nhập UUID thủ công.
   - Toast thông báo kết quả thao tác trực quan.

4. **Bot 4 (Git & GitHub Deployment)**: 
   - Dọn dẹp tracking các file rác (target, zip), tạo `.gitignore` chuẩn.
   - Commit toàn bộ thay đổi với thông điệp Conventional Commits chi tiết.
   - Đã push thành công lên remote `origin/n5` tại: https://github.com/Hoc04/quanlydaotaoit24m/tree/n5.
   - Sẵn sàng tạo Pull Request đến repo gốc `upstream` (https://github.com/truongvanhieu/quanlydaotaoit24m/tree/n5).

5. **Bot 5 (Supervisor, Testing & Quality Assurance)**: 
   - Viết bộ kiểm thử tự động `N5ServiceTests` bao phủ toàn bộ luồng tạo Niên khóa, Năm học, Học kỳ, Lớp học phần, Sinh viên, Đăng ký học phần (với test case bắt lỗi trùng đăng ký) và Phân công giảng viên.
   - Kiểm thử Maven Build: `BUILD SUCCESS` (4/4 tests passed, 0 failures, 0 errors).

## Phases
- [x] Phase 1: Phân tích tài liệu Excel và đối chiếu codebase hiện tại
- [x] Phase 2: Bot 1 & Bot 2 - Chuẩn hóa Backend Entity, Repository, Service, Controller
- [x] Phase 3: Bot 3 - Thiết kế và hoàn thiện Web UI/UX cho N5
- [x] Phase 4: Bot 5 - Kiểm thử Maven Compile, API Test, sửa lỗi phát sinh
- [x] Phase 5: Bot 4 - Commit Git và đẩy lên nhánh n5 trên GitHub
