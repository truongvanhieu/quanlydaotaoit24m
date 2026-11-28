DECLARE @TrainingPermissions TABLE (
    code nvarchar(100) NOT NULL,
    name nvarchar(150) NOT NULL,
    module nvarchar(100) NOT NULL,
    description nvarchar(255) NOT NULL
);

INSERT INTO @TrainingPermissions (code, name, module, description)
VALUES
    (N'STUDENT_IMPORT', N'Import danh sách sinh viên', N'STUDENTS', N'Nhập danh sách sinh viên từ tệp dữ liệu'),
    (N'STUDENT_EXPORT', N'Xuất danh sách sinh viên', N'STUDENTS', N'Xuất danh sách sinh viên ra tệp'),
    (N'STUDENT_STATUS_MANAGE', N'Cập nhật trạng thái sinh viên', N'STUDENTS', N'Quản lý trạng thái học tập và bảo lưu'),
    (N'ACADEMIC_YEAR_VIEW', N'Xem năm học', N'ACADEMIC', N'Tra cứu danh sách năm học'),
    (N'ACADEMIC_YEAR_MANAGE', N'Quản lý năm học', N'ACADEMIC', N'Tạo và cập nhật năm học'),
    (N'SEMESTER_VIEW', N'Xem học kỳ', N'ACADEMIC', N'Tra cứu học kỳ và thời gian đào tạo'),
    (N'SEMESTER_MANAGE', N'Quản lý học kỳ', N'ACADEMIC', N'Tạo và cập nhật học kỳ'),
    (N'DEPARTMENT_VIEW', N'Xem khoa', N'ACADEMIC', N'Tra cứu danh sách khoa'),
    (N'DEPARTMENT_MANAGE', N'Quản lý khoa', N'ACADEMIC', N'Tạo và cập nhật khoa'),
    (N'MAJOR_VIEW', N'Xem ngành đào tạo', N'ACADEMIC', N'Tra cứu ngành và chương trình đào tạo'),
    (N'MAJOR_MANAGE', N'Quản lý ngành đào tạo', N'ACADEMIC', N'Tạo và cập nhật ngành đào tạo'),
    (N'CLASS_VIEW', N'Xem lớp hành chính', N'CLASSES', N'Tra cứu lớp hành chính và danh sách sinh viên'),
    (N'CLASS_MANAGE', N'Quản lý lớp hành chính', N'CLASSES', N'Tạo và cập nhật lớp hành chính'),
    (N'CURRICULUM_VIEW', N'Xem chương trình đào tạo', N'CURRICULUM', N'Tra cứu chương trình và khung học tập'),
    (N'CURRICULUM_MANAGE', N'Quản lý chương trình đào tạo', N'CURRICULUM', N'Tạo và cập nhật chương trình đào tạo'),
    (N'REGISTRATION_VIEW', N'Xem đăng ký học phần', N'REGISTRATION', N'Tra cứu đợt và kết quả đăng ký học phần'),
    (N'REGISTRATION_MANAGE', N'Mở đợt đăng ký học phần', N'REGISTRATION', N'Tạo và cấu hình đợt đăng ký học phần'),
    (N'REGISTRATION_APPROVE', N'Duyệt đăng ký học phần', N'REGISTRATION', N'Duyệt hoặc từ chối yêu cầu đăng ký'),
    (N'ENROLLMENT_VIEW', N'Xem danh sách lớp học phần', N'ENROLLMENT', N'Tra cứu danh sách sinh viên của lớp học phần'),
    (N'ENROLLMENT_MANAGE', N'Quản lý danh sách lớp học phần', N'ENROLLMENT', N'Thêm hoặc cập nhật sinh viên trong lớp học phần'),
    (N'SCHEDULE_VIEW', N'Xem thời khóa biểu', N'SCHEDULE', N'Tra cứu thời khóa biểu và lịch giảng dạy'),
    (N'SCHEDULE_MANAGE', N'Quản lý thời khóa biểu', N'SCHEDULE', N'Sắp xếp và cập nhật lịch học'),
    (N'ROOM_VIEW', N'Xem phòng học', N'FACILITIES', N'Tra cứu phòng học và tình trạng sử dụng'),
    (N'ROOM_MANAGE', N'Quản lý phòng học', N'FACILITIES', N'Tạo và cập nhật phòng học'),
    (N'ATTENDANCE_VIEW', N'Xem điểm danh', N'ATTENDANCE', N'Tra cứu chuyên cần của lớp học phần'),
    (N'ATTENDANCE_TAKE', N'Ghi nhận điểm danh', N'ATTENDANCE', N'Điểm danh sinh viên theo buổi học'),
    (N'GRADE_EXPORT', N'Xuất bảng điểm', N'GRADES', N'Xuất bảng điểm của lớp học phần'),
    (N'EXAM_APPROVE', N'Duyệt lịch thi', N'EXAMS', N'Kiểm tra và duyệt lịch thi'),
    (N'TRANSCRIPT_VIEW', N'Xem bảng điểm tích lũy', N'TRANSCRIPTS', N'Tra cứu kết quả học tập tích lũy'),
    (N'TRANSCRIPT_EXPORT', N'Xuất bảng điểm tích lũy', N'TRANSCRIPTS', N'Xuất bảng điểm tích lũy của sinh viên'),
    (N'TUITION_INVOICE', N'Lập khoản thu học phí', N'TUITION', N'Tạo khoản phải thu học phí cho sinh viên'),
    (N'TUITION_REFUND', N'Xử lý hoàn học phí', N'TUITION', N'Ghi nhận và xử lý yêu cầu hoàn học phí'),
    (N'REPORT_MANAGE', N'Quản lý báo cáo đào tạo', N'REPORTS', N'Tạo và cấu hình báo cáo đào tạo');

INSERT INTO dbo.permissions (
    id,
    code,
    name,
    module,
    description,
    created_at,
    is_active
)
SELECT
    NEWID(),
    trainingPermission.code,
    trainingPermission.name,
    trainingPermission.module,
    trainingPermission.description,
    SYSUTCDATETIME(),
    1
FROM @TrainingPermissions AS trainingPermission
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.permissions AS existingPermission
    WHERE existingPermission.code = trainingPermission.code
      AND existingPermission.deleted_at IS NULL
);
GO
