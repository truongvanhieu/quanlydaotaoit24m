USE [it24m];
GO
-- N6: KIỂM TRA KHÔNG PHÁ HỦY DỮ LIỆU, KHÔNG CHẠY LẠI CREATE TABLE
SELECT t.name AS table_name
FROM sys.tables t WHERE t.name IN (
  'registration_periods','course_registrations','course_equivalences',
  'courses','courses_classes','semesters','students_'
) ORDER BY t.name;
GO
SELECT 'registration_periods' table_name, COUNT(*) total FROM dbo.registration_periods
UNION ALL SELECT 'course_registrations', COUNT(*) FROM dbo.course_registrations
UNION ALL SELECT 'course_equivalences', COUNT(*) FROM dbo.course_equivalences
UNION ALL SELECT 'courses', COUNT(*) FROM dbo.courses
UNION ALL SELECT 'courses_classes', COUNT(*) FROM dbo.courses_classes
UNION ALL SELECT 'semesters', COUNT(*) FROM dbo.semesters
UNION ALL SELECT 'students_', COUNT(*) FROM dbo.students_;
GO
-- Kiểm tra thiết kế khóa ngoại trước khi bổ sung dữ liệu
SELECT fk.name, OBJECT_NAME(fk.parent_object_id) AS child_table,
       OBJECT_NAME(fk.referenced_object_id) AS parent_table
FROM sys.foreign_keys fk
WHERE OBJECT_NAME(fk.parent_object_id) IN ('registration_periods','course_registrations','course_equivalences');
GO
