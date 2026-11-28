USE [it24m];
GO
SET ANSI_NULLS ON;
GO
SET QUOTED_IDENTIFIER ON;
GO
SET NOCOUNT ON;
GO

-- ============================================================================
-- NHOM 2 SAMPLE SEED DATA - QUAN LY SINH VIEN & LOP HANH CHINH
-- Gồm: 5 Trạng thái, 5 Lớp hành chính, 6 Phân công CVHT,
--      40 Sinh viên, 42 Phân lớp (gồm 2 lịch sử chuyển lớp),
--      52 Lịch sử trạng thái (đồng bộ 100% với students.status & 7 FK)
-- ============================================================================

BEGIN TRAN;

-- 1. Xóa dữ liệu cũ theo đúng thứ tự bảng con -> bảng cha
DELETE FROM [dbo].[student_status_histories];
DELETE FROM [dbo].[student_status];
DELETE FROM [dbo].[advisor_classe_sections];
DELETE FROM [dbo].[student_classe_sections];
DELETE FROM [dbo].[students];
DELETE FROM [dbo].[student_classes];

-- 2. Khai báo ID danh mục dùng chung (Niên khóa, Khoa, Ngành, CTĐT, CVHT)
DECLARE @Year2023 UNIQUEIDENTIFIER = '11111111-1111-4111-8111-000000002023';
DECLARE @Year2024 UNIQUEIDENTIFIER = '11111111-1111-4111-8111-000000002024';
DECLARE @DeptCNTT UNIQUEIDENTIFIER = '22222222-2222-4222-8222-000000000001';
DECLARE @MajorCNTT UNIQUEIDENTIFIER = '33333333-3333-4333-8333-000000000001';
DECLARE @MajorKTPM UNIQUEIDENTIFIER = '33333333-3333-4333-8333-000000000002';
DECLARE @MajorAI   UNIQUEIDENTIFIER = '33333333-3333-4333-8333-000000000003';
DECLARE @MajorDS   UNIQUEIDENTIFIER = '33333333-3333-4333-8333-000000000004';
DECLARE @ProgCNTT  UNIQUEIDENTIFIER = '44444444-4444-4444-8444-000000000001';
DECLARE @ProgKTPM  UNIQUEIDENTIFIER = '44444444-4444-4444-8444-000000000002';
DECLARE @ProgAI    UNIQUEIDENTIFIER = '44444444-4444-4444-8444-000000000003';
DECLARE @ProgDS    UNIQUEIDENTIFIER = '44444444-4444-4444-8444-000000000004';

DECLARE @Adv1 UNIQUEIDENTIFIER = '55555555-5555-4555-8555-000000000001';
DECLARE @Adv2 UNIQUEIDENTIFIER = '55555555-5555-4555-8555-000000000002';
DECLARE @Adv3 UNIQUEIDENTIFIER = '55555555-5555-4555-8555-000000000003';
DECLARE @Adv4 UNIQUEIDENTIFIER = '55555555-5555-4555-8555-000000000004';
DECLARE @Adv5 UNIQUEIDENTIFIER = '55555555-5555-4555-8555-000000000005';

-- 3. Chèn 5 Lớp hành chính (student_classes)
DECLARE @ClsIT24M UNIQUEIDENTIFIER = NEWID();
DECLARE @ClsST23A UNIQUEIDENTIFIER = NEWID();
DECLARE @ClsST23B UNIQUEIDENTIFIER = NEWID();
DECLARE @ClsAI24A UNIQUEIDENTIFIER = NEWID();
DECLARE @ClsDS24A UNIQUEIDENTIFIER = NEWID();

INSERT INTO [dbo].[student_classes]
    ([id], [code], [name], [academic_year_id], [department_id], [major_id], [training_program_id], [advisor_id], [is_active])
VALUES
    (@ClsIT24M, N'IT24M', N'Lớp Công nghệ thông tin K24M', @Year2024, @DeptCNTT, @MajorCNTT, @ProgCNTT, @Adv1, 1),
    (@ClsST23A, N'ST23A', N'Lớp Kỹ thuật phần mềm K23A',   @Year2023, @DeptCNTT, @MajorKTPM, @ProgKTPM, @Adv2, 1),
    (@ClsST23B, N'ST23B', N'Lớp Kỹ thuật phần mềm K23B',   @Year2023, @DeptCNTT, @MajorKTPM, @ProgKTPM, @Adv3, 1),
    (@ClsAI24A, N'AI24A', N'Lớp Trí tuệ nhân tạo K24A',    @Year2024, @DeptCNTT, @MajorAI,   @ProgAI,   @Adv4, 1),
    (@ClsDS24A, N'DS24A', N'Lớp Khoa học dữ liệu K24A',    @Year2024, @DeptCNTT, @MajorDS,   @ProgDS,   @Adv5, 1);

-- 4. Chèn 6 Phân công CVHT (advisor_classe_sections - có 1 lịch sử bàn giao CVHT)
INSERT INTO [dbo].[advisor_classe_sections]
    ([id], [employee_id], [student_classe_id], [start_date], [end_date], [is_active])
VALUES
    (NEWID(), @Adv5, @ClsIT24M, '2024-09-01', '2025-08-31', 0), -- CVHT cũ của IT24M
    (NEWID(), @Adv1, @ClsIT24M, '2025-09-01', NULL,         1), -- CVHT hiện tại của IT24M
    (NEWID(), @Adv2, @ClsST23A, '2023-09-01', NULL,         1),
    (NEWID(), @Adv3, @ClsST23B, '2023-09-01', NULL,         1),
    (NEWID(), @Adv4, @ClsAI24A, '2024-09-01', NULL,         1),
    (NEWID(), @Adv5, @ClsDS24A, '2024-09-01', NULL,         1);

-- 5. Chèn 5 Danh mục Trạng thái sinh viên (student_status)
DECLARE @StDangHoc   UNIQUEIDENTIFIER = NEWID();
DECLARE @StBaoLuu    UNIQUEIDENTIFIER = NEWID();
DECLARE @StTamDung   UNIQUEIDENTIFIER = NEWID();
DECLARE @StThoiHoc   UNIQUEIDENTIFIER = NEWID();
DECLARE @StTotNghiep UNIQUEIDENTIFIER = NEWID();

INSERT INTO [dbo].[student_status]
    ([id], [student_id], [code], [name], [description], [is_active])
VALUES
    (@StDangHoc,   NULL, N'DANG_HOC',   N'Đang học',      N'Sinh viên đang theo học bình thường theo tiến độ đào tạo', 1),
    (@StBaoLuu,    NULL, N'BAO_LUU',    N'Bảo lưu',       N'Sinh viên được duyệt bảo lưu kết quả học tập có thời hạn', 1),
    (@StTamDung,   NULL, N'TAM_DUNG',   N'Tạm dừng',      N'Sinh viên tạm dừng học tập theo đơn cá nhân hoặc học vụ', 1),
    (@StThoiHoc,   NULL, N'THOI_HOC',   N'Thôi học',      N'Sinh viên đã có quyết định thôi học chính thức', 1),
    (@StTotNghiep, NULL, N'TOT_NGHIEP', N'Đã tốt nghiệp', N'Sinh viên đã hoàn thành chương trình và được công nhận tốt nghiệp', 1);

-- 6. Bảng tạm chứa 40 sinh viên thực tế
DECLARE @SeedStudents TABLE (
    [id] UNIQUEIDENTIFIER,
    [code] VARCHAR(100),
    [full_name] NVARCHAR(255),
    [dob] DATE,
    [gender] NVARCHAR(10),
    [cccd] VARCHAR(20),
    [issue_date] DATE,
    [card_place] NVARCHAR(100),
    [address] NVARCHAR(300),
    [current_address] NVARCHAR(300),
    [year_id] UNIQUEIDENTIFIER,
    [major_id] UNIQUEIDENTIFIER,
    [prog_id] UNIQUEIDENTIFIER,
    [status_code] VARCHAR(50),
    [class_id] UNIQUEIDENTIFIER,
    [admission_year] DATETIME2(7),
    [reason] NVARCHAR(255),
    [decision_no] NVARCHAR(255),
    [decision_date] DATE
);

INSERT INTO @SeedStudents VALUES
-- Lớp IT24M (8 SV)
(NEWID(),'SV24001',N'Nguyễn Văn An',     '2002-03-12',N'Nam','048202001101','2021-05-10',N'Cục CSQLHC về TTXH',N'Hải Châu, Đà Nẵng',       N'33 Xô Viết Nghệ Tĩnh, Đà Nẵng',  @Year2024,@MajorCNTT,@ProgCNTT,'DANG_HOC',  @ClsIT24M,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24002',N'Trần Thị Bích Ngọc','2002-07-24',N'Nữ', '048302001102','2021-06-15',N'Cục CSQLHC về TTXH',N'Thanh Khê, Đà Nẵng',      N'120 Điện Biên Phủ, Đà Nẵng',     @Year2024,@MajorCNTT,@ProgCNTT,'DANG_HOC',  @ClsIT24M,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24003',N'Lê Minh Khoa',      '2001-11-08',N'Nam','049201001103','2021-04-20',N'Cục CSQLHC về TTXH',N'Tam Kỳ, Quảng Nam',       N'45 Núi Thành, Đà Nẵng',          @Year2024,@MajorCNTT,@ProgCNTT,'DANG_HOC',  @ClsIT24M,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24004',N'Phạm Thu Hà',       '2002-01-19',N'Nữ', '046302001104','2021-08-02',N'Cục CSQLHC về TTXH',N'TP. Huế, Thừa Thiên Huế', N'88 Lê Thanh Nghị, Đà Nẵng',      @Year2024,@MajorCNTT,@ProgCNTT,'BAO_LUU',   @ClsIT24M,'2024-09-05',N'Bảo lưu 1 học kỳ vì lý do sức khỏe',    N'QĐ-2025/BL-01','2025-08-20'),
(NEWID(),'SV24005',N'Hoàng Đức Mạnh',    '2001-09-30',N'Nam','048201001105','2021-05-12',N'Cục CSQLHC về TTXH',N'Sơn Trà, Đà Nẵng',        N'19 Ngô Quyền, Đà Nẵng',          @Year2024,@MajorCNTT,@ProgCNTT,'DANG_HOC',  @ClsIT24M,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24006',N'Võ Thị Mỹ Linh',    '2002-05-14',N'Nữ', '051302001106','2021-07-08',N'Cục CSQLHC về TTXH',N'TP. Quảng Ngãi',          N'72 Phan Đăng Lưu, Đà Nẵng',      @Year2024,@MajorCNTT,@ProgCNTT,'DANG_HOC',  @ClsIT24M,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24007',N'Đặng Quốc Bảo',     '2001-12-03',N'Nam','049201001107','2021-09-11',N'Cục CSQLHC về TTXH',N'Hội An, Quảng Nam',       N'15 Tiểu La, Đà Nẵng',            @Year2024,@MajorCNTT,@ProgCNTT,'TAM_DUNG',  @ClsIT24M,'2024-09-05',N'Tạm dừng học tập giải quyết việc gia đình',N'QĐ-2025/TD-01','2025-10-10'),
(NEWID(),'SV24008',N'Bùi Thanh Thảo',    '2002-04-27',N'Nữ', '048302001108','2021-05-22',N'Cục CSQLHC về TTXH',N'Cẩm Lệ, Đà Nẵng',         N'210 Cách Mạng Tháng 8, Đà Nẵng', @Year2024,@MajorCNTT,@ProgCNTT,'DANG_HOC',  @ClsIT24M,'2024-09-05',NULL,NULL,NULL),

-- Lớp ST23A (8 SV)
(NEWID(),'SV23001',N'Nguyễn Ngọc Minh',  '2005-02-10',N'Nam','048205002101','2022-03-14',N'Cục CSQLHC về TTXH',N'Hải Châu, Đà Nẵng',       N'54 Quang Trung, Đà Nẵng',        @Year2023,@MajorKTPM,@ProgKTPM,'DANG_HOC',  @ClsST23A,'2023-09-05',NULL,NULL,NULL),
(NEWID(),'SV23002',N'Trương Nam Phong',  '2005-06-18',N'Nam','049205002102','2022-04-19',N'Cục CSQLHC về TTXH',N'Điện Bàn, Quảng Nam',     N'90 Nguyễn Hữu Thọ, Đà Nẵng',     @Year2023,@MajorKTPM,@ProgKTPM,'DANG_HOC',  @ClsST23A,'2023-09-05',NULL,NULL,NULL),
(NEWID(),'SV23003',N'Đỗ Thị Lan Anh',    '2005-08-22',N'Nữ', '048305002103','2022-05-25',N'Cục CSQLHC về TTXH',N'Ngũ Hành Sơn, Đà Nẵng',   N'14 Lê Văn Hiến, Đà Nẵng',        @Year2023,@MajorKTPM,@ProgKTPM,'TOT_NGHIEP',@ClsST23A,'2023-09-05',N'Hoàn thành vượt tiến độ CTĐT',           N'QĐ-2026/TN-01','2026-06-15'),
(NEWID(),'SV23004',N'Phan Văn Hùng',     '2005-01-05',N'Nam','045205002104','2022-02-10',N'Cục CSQLHC về TTXH',N'Đông Hà, Quảng Trị',      N'31 Hòa Cường Bắc, Đà Nẵng',      @Year2023,@MajorKTPM,@ProgKTPM,'DANG_HOC',  @ClsST23A,'2023-09-05',NULL,NULL,NULL),
(NEWID(),'SV23005',N'Ngô Thị Yến Nhi',   '2005-10-11',N'Nữ', '052305002105','2022-07-01',N'Cục CSQLHC về TTXH',N'Quy Nhơn, Bình Định',     N'66 Tố Hữu, Đà Nẵng',             @Year2023,@MajorKTPM,@ProgKTPM,'DANG_HOC',  @ClsST23A,'2023-09-05',NULL,NULL,NULL),
(NEWID(),'SV23006',N'Vũ Đình Trung',     '2005-04-16',N'Nam','044205002106','2022-06-09',N'Cục CSQLHC về TTXH',N'Đồng Hới, Quảng Bình',    N'102 Nguyễn Tri Phương, Đà Nẵng', @Year2023,@MajorKTPM,@ProgKTPM,'BAO_LUU',   @ClsST23A,'2023-09-05',N'Bảo lưu thực hiện nghĩa vụ quân sự',     N'QĐ-2025/BL-02','2025-02-15'),
(NEWID(),'SV23007',N'Lý Gia Hân',        '2005-09-28',N'Nữ', '048305002107','2022-08-12',N'Cục CSQLHC về TTXH',N'Liên Chiểu, Đà Nẵng',     N'48 Tôn Đức Thắng, Đà Nẵng',      @Year2023,@MajorKTPM,@ProgKTPM,'DANG_HOC',  @ClsST23A,'2023-09-05',NULL,NULL,NULL),
(NEWID(),'SV23008',N'Huỳnh Tấn Phát',    '2005-12-09',N'Nam','049205002108','2022-09-03',N'Cục CSQLHC về TTXH',N'Thăng Bình, Quảng Nam',   N'23 Lương Nhữ Hộc, Đà Nẵng',      @Year2023,@MajorKTPM,@ProgKTPM,'THOI_HOC',  @ClsST23A,'2023-09-05',N'Thôi học theo nguyện vọng cá nhân',      N'QĐ-2025/TH-01','2025-11-05'),

-- Lớp ST23B (8 SV)
(NEWID(),'SV23009',N'Trịnh Quang Huy',   '2005-03-04',N'Nam','048205002201','2022-03-20',N'Cục CSQLHC về TTXH',N'Hải Châu, Đà Nẵng',       N'18 Ông Ích Khiêm, Đà Nẵng',      @Year2023,@MajorKTPM,@ProgKTPM,'DANG_HOC',  @ClsST23B,'2023-09-05',NULL,NULL,NULL),
(NEWID(),'SV23010',N'Mai Thị Kim Oanh',  '2005-07-15',N'Nữ', '051305002202','2022-04-28',N'Cục CSQLHC về TTXH',N'Đức Phổ, Quảng Ngãi',     N'77 Phan Tứ, Đà Nẵng',            @Year2023,@MajorKTPM,@ProgKTPM,'DANG_HOC',  @ClsST23B,'2023-09-05',NULL,NULL,NULL),
(NEWID(),'SV23011',N'Dương Văn Tài',     '2005-05-21',N'Nam','046205002203','2022-05-11',N'Cục CSQLHC về TTXH',N'Hương Thủy, Huế',         N'115 Núi Thành, Đà Nẵng',         @Year2023,@MajorKTPM,@ProgKTPM,'TOT_NGHIEP',@ClsST23B,'2023-09-05',N'Đủ điều kiện tốt nghiệp loại Giỏi',      N'QĐ-2026/TN-02','2026-06-15'),
(NEWID(),'SV23012',N'Cao Thị Hồng Nhung','2005-11-17',N'Nữ', '048305002204','2022-06-22',N'Cục CSQLHC về TTXH',N'Thanh Khê, Đà Nẵng',      N'29 Hà Huy Tập, Đà Nẵng',         @Year2023,@MajorKTPM,@ProgKTPM,'DANG_HOC',  @ClsST23B,'2023-09-05',NULL,NULL,NULL),
(NEWID(),'SV23013',N'Lâm Hoàng Long',    '2005-02-26',N'Nam','064205002205','2022-07-14',N'Cục CSQLHC về TTXH',N'Pleiku, Gia Lai',         N'82 Châu Thị Vĩnh Tế, Đà Nẵng',   @Year2023,@MajorKTPM,@ProgKTPM,'DANG_HOC',  @ClsST23B,'2023-09-05',NULL,NULL,NULL),
(NEWID(),'SV23014',N'Tạ Minh Thư',       '2005-08-03',N'Nữ', '066305002206','2022-08-09',N'Cục CSQLHC về TTXH',N'Buôn Ma Thuột, Đắk Lắk',  N'41 Ngũ Hành Sơn, Đà Nẵng',       @Year2023,@MajorKTPM,@ProgKTPM,'BAO_LUU',   @ClsST23B,'2023-09-05',N'Bảo lưu đi thực tập doanh nghiệp nước ngoài',N'QĐ-2025/BL-03','2025-09-10'),
(NEWID(),'SV23015',N'Châu Vĩnh Khang',   '2005-10-30',N'Nam','049205002207','2022-09-18',N'Cục CSQLHC về TTXH',N'Duy Xuyên, Quảng Nam',    N'16 Hoàng Diệu, Đà Nẵng',         @Year2023,@MajorKTPM,@ProgKTPM,'DANG_HOC',  @ClsST23B,'2023-09-05',NULL,NULL,NULL),
(NEWID(),'SV23016',N'Đinh Thị Quỳnh Như','2005-06-08',N'Nữ', '048305002208','2022-05-04',N'Cục CSQLHC về TTXH',N'Sơn Trà, Đà Nẵng',        N'55 Phạm Văn Đồng, Đà Nẵng',      @Year2023,@MajorKTPM,@ProgKTPM,'TOT_NGHIEP',@ClsST23B,'2023-09-05',N'Đủ điều kiện tốt nghiệp loại Xuất sắc',  N'QĐ-2026/TN-03','2026-06-15'),

-- Lớp AI24A (8 SV)
(NEWID(),'SV24009',N'Nguyễn Thanh Tùng', '2006-01-14',N'Nam','048206003101','2023-02-12',N'Cục CSQLHC về TTXH',N'Hải Châu, Đà Nẵng',       N'10 Trần Phú, Đà Nẵng',           @Year2024,@MajorAI,  @ProgAI,  'DANG_HOC',  @ClsAI24A,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24010',N'Phạm Ngọc Diệp',    '2006-04-22',N'Nữ', '049306003102','2023-03-18',N'Cục CSQLHC về TTXH',N'Đại Lộc, Quảng Nam',      N'63 Xô Viết Nghệ Tĩnh, Đà Nẵng',  @Year2024,@MajorAI,  @ProgAI,  'DANG_HOC',  @ClsAI24A,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24011',N'Lê Hữu Phước',      '2006-07-09',N'Nam','052206003103','2023-04-25',N'Cục CSQLHC về TTXH',N'An Nhơn, Bình Định',      N'27 Lê Thanh Nghị, Đà Nẵng',      @Year2024,@MajorAI,  @ProgAI,  'DANG_HOC',  @ClsAI24A,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24012',N'Trần Khánh Vy',     '2006-09-17',N'Nữ', '048306003104','2023-05-30',N'Cục CSQLHC về TTXH',N'Cẩm Lệ, Đà Nẵng',         N'85 Tôn Đản, Đà Nẵng',            @Year2024,@MajorAI,  @ProgAI,  'DANG_HOC',  @ClsAI24A,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24013',N'Bùi Nhật Nam',      '2006-11-25',N'Nam','046206003105','2023-06-14',N'Cục CSQLHC về TTXH',N'Phong Điền, Huế',         N'109 Nguyễn Văn Linh, Đà Nẵng',   @Year2024,@MajorAI,  @ProgAI,  'BAO_LUU',   @ClsAI24A,'2024-09-05',N'Bảo lưu kết quả học tập 1 năm học',      N'QĐ-2025/BL-04','2025-09-01'),
(NEWID(),'SV24014',N'Võ Hoàng Anh',      '2006-03-29',N'Nam','051206003106','2023-07-21',N'Cục CSQLHC về TTXH',N'Sơn Tịnh, Quảng Ngãi',    N'44 Phan Châu Trinh, Đà Nẵng',    @Year2024,@MajorAI,  @ProgAI,  'DANG_HOC',  @ClsAI24A,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24015',N'Đoàn Thị Mai Phương','2006-08-12',N'Nữ','048306003107','2023-08-05',N'Cục CSQLHC về TTXH',N'Liên Chiểu, Đà Nẵng',     N'12 Nguyễn Lương Bằng, Đà Nẵng',  @Year2024,@MajorAI,  @ProgAI,  'DANG_HOC',  @ClsAI24A,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24016',N'Hồ Quốc Việt',      '2006-05-06',N'Nam','044206003108','2023-09-11',N'Cục CSQLHC về TTXH',N'Ba Đồn, Quảng Bình',      N'38 Tiểu La, Đà Nẵng',            @Year2024,@MajorAI,  @ProgAI,  'THOI_HOC',  @ClsAI24A,'2024-09-05',N'Chuyển hướng du học nước ngoài',         N'QĐ-2026/TH-02','2026-01-20'),

-- Lớp DS24A (8 SV)
(NEWID(),'SV24017',N'Nguyễn Gia Bảo',    '2006-02-18',N'Nam','048206004101','2023-03-08',N'Cục CSQLHC về TTXH',N'Hải Châu, Đà Nẵng',       N'75 Hùng Vương, Đà Nẵng',         @Year2024,@MajorDS,  @ProgDS,  'DANG_HOC',  @ClsDS24A,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24018',N'Lê Thị Bảo Trâm',   '2006-06-27',N'Nữ', '049306004102','2023-04-12',N'Cục CSQLHC về TTXH',N'Quế Sơn, Quảng Nam',      N'51 Núi Thành, Đà Nẵng',          @Year2024,@MajorDS,  @ProgDS,  'DANG_HOC',  @ClsDS24A,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24019',N'Trần Đức Anh',      '2006-10-03',N'Nam','054206004103','2023-05-19',N'Cục CSQLHC về TTXH',N'Tuy Hòa, Phú Yên',        N'22 Lê Duẩn, Đà Nẵng',            @Year2024,@MajorDS,  @ProgDS,  'DANG_HOC',  @ClsDS24A,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24020',N'Phạm Thị Thanh Tâm','2006-01-25',N'Nữ', '048306004104','2023-06-24',N'Cục CSQLHC về TTXH',N'Ngũ Hành Sơn, Đà Nẵng',   N'99 Võ Nguyên Giáp, Đà Nẵng',     @Year2024,@MajorDS,  @ProgDS,  'BAO_LUU',   @ClsDS24A,'2024-09-05',N'Bảo lưu điều trị y tế',                  N'QĐ-2026/BL-05','2026-02-10'),
(NEWID(),'SV24021',N'Hoàng Minh Khôi',   '2006-04-11',N'Nam','045206004105','2023-07-02',N'Cục CSQLHC về TTXH',N'Vĩnh Linh, Quảng Trị',    N'140 Trưng Nữ Vương, Đà Nẵng',    @Year2024,@MajorDS,  @ProgDS,  'DANG_HOC',  @ClsDS24A,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24022',N'Nguyễn Thùy Dương', '2006-09-05',N'Nữ', '062306004106','2023-08-16',N'Cục CSQLHC về TTXH',N'TP. Kon Tum',             N'67 Lê Thanh Nghị, Đà Nẵng',      @Year2024,@MajorDS,  @ProgDS,  'DANG_HOC',  @ClsDS24A,'2024-09-05',NULL,NULL,NULL),
(NEWID(),'SV24023',N'Phan Tấn Đạt',      '2006-12-19',N'Nam','048206004107','2023-09-22',N'Cục CSQLHC về TTXH',N'Hòa Vang, Đà Nẵng',       N'18 Trường Chinh, Đà Nẵng',       @Year2024,@MajorDS,  @ProgDS,  'TAM_DUNG',  @ClsDS24A,'2024-09-05',N'Tạm dừng 1 học kỳ hoàn thiện hồ sơ',     N'QĐ-2026/TD-02','2026-03-01'),
(NEWID(),'SV24024',N'Đặng Thị Hồng Hạnh','2006-07-31',N'Nữ', '049306004108','2023-06-10',N'Cục CSQLHC về TTXH',N'Núi Thành, Quảng Nam',    N'30 Xô Viết Nghệ Tĩnh, Đà Nẵng',  @Year2024,@MajorDS,  @ProgDS,  'DANG_HOC',  @ClsDS24A,'2024-09-05',NULL,NULL,NULL);

-- 7. Chèn 40 Sinh viên vào bảng students
INSERT INTO [dbo].[students] (
    [id], [code], [full_name], [date_of_birth], [gender],
    [personal_identification_number], [date_of_issue], [card_place],
    [address], [current_address], [academic_year_year], [department_id],
    [major_id], [training_program_id], [status], [student_classess_id],
    [admission_year], [is_active]
)
SELECT
    [id], [code], [full_name], [dob], [gender],
    [cccd], [issue_date], [card_place],
    [address], [current_address], [year_id], @DeptCNTT,
    [major_id], [prog_id], [status_code], [class_id],
    [admission_year], 1
FROM @SeedStudents;

-- 8. Chèn 40 Phân lớp hiện tại vào student_classe_sections
INSERT INTO [dbo].[student_classe_sections]
    ([id], [student_id], [student_classe_id], [status], [note], [start_date], [end_date], [is_active])
SELECT
    NEWID(),
    [id],
    [class_id],
    CASE [status_code]
        WHEN 'TOT_NGHIEP' THEN 'completed'
        WHEN 'THOI_HOC'   THEN 'dropped'
        ELSE 'studying'
    END,
    N'Phân lớp hành chính đầu khóa',
    [admission_year],
    CASE WHEN [status_code] IN ('TOT_NGHIEP','THOI_HOC') THEN CAST([decision_date] AS DATETIME2(7)) ELSE NULL END,
    1
FROM @SeedStudents;

-- Bổ sung 2 bản ghi lịch sử chuyển lớp (để demo lịch sử phân lớp nhiều mốc)
INSERT INTO [dbo].[student_classe_sections]
    ([id], [student_id], [student_classe_id], [status], [note], [start_date], [end_date], [is_active])
SELECT NEWID(), [id], @ClsST23B, 'transferred', N'Chuyển từ lớp ST23B sang lớp ST23A theo nguyện vọng', '2023-09-05', '2024-01-15', 0
FROM @SeedStudents WHERE [code] = 'SV23001';

INSERT INTO [dbo].[student_classe_sections]
    ([id], [student_id], [student_classe_id], [status], [note], [start_date], [end_date], [is_active])
SELECT NEWID(), [id], @ClsDS24A, 'transferred', N'Chuyển từ lớp DS24A sang lớp AI24A đầu học kỳ 2', '2024-09-05', '2025-01-10', 0
FROM @SeedStudents WHERE [code] = 'SV24009';

-- 9. Chèn Lịch sử trạng thái ban đầu (Nhập học - DANG_HOC) cho tất cả 40 sinh viên
INSERT INTO [dbo].[student_status_histories]
    ([id], [student_id], [status_id], [start_date], [end_date], [reason], [decision_number], [decision_date], [note], [is_active])
SELECT
    NEWID(),
    [id],
    @StDangHoc,
    CAST([admission_year] AS DATE),
    CASE WHEN [status_code] = 'DANG_HOC' THEN NULL ELSE [decision_date] END,
    N'Nhập học chính thức đầu khóa',
    N'QĐ-' + CAST(YEAR([admission_year]) AS NVARCHAR(4)) + N'/NH-' + RIGHT([code], 3),
    DATEADD(DAY, -4, CAST([admission_year] AS DATE)),
    N'Khởi tạo trạng thái khi nhập học',
    1
FROM @SeedStudents;

-- 10. Chèn 12 bản ghi Lịch sử chuyển sang trạng thái mới (BAO_LUU / TAM_DUNG / THOI_HOC / TOT_NGHIEP)
INSERT INTO [dbo].[student_status_histories]
    ([id], [student_id], [status_id], [start_date], [end_date], [reason], [decision_number], [decision_date], [note], [is_active])
SELECT
    NEWID(),
    [id],
    CASE [status_code]
        WHEN 'BAO_LUU'    THEN @StBaoLuu
        WHEN 'TAM_DUNG'   THEN @StTamDung
        WHEN 'THOI_HOC'   THEN @StThoiHoc
        WHEN 'TOT_NGHIEP' THEN @StTotNghiep
    END,
    [decision_date],
    NULL,
    [reason],
    [decision_no],
    [decision_date],
    N'Cập nhật theo quyết định học vụ',
    1
FROM @SeedStudents
WHERE [status_code] <> 'DANG_HOC';

COMMIT TRAN;
PRINT N'Chèn dữ liệu mẫu Nhóm 2 thành công!';
GO

