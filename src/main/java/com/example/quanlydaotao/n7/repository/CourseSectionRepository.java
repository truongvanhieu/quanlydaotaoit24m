package com.example.quanlydaotao.n7.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.quanlydaotao.n7.entity.CourseSection;

public interface CourseSectionRepository extends JpaRepository<CourseSection, UUID> {

    @Query("""
            select s from CourseSection s left join s.course c
            where s.deletedAt is null and (:active is null or s.active = :active)
              and (:keyword is null or lower(coalesce(s.code, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(s.name, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(c.code, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(c.name, '')) like lower(concat('%', :keyword, '%')))
            order by s.code
            """)
    List<CourseSection> search(@Param("keyword") String keyword, @Param("active") Boolean active);

    boolean existsByCodeIgnoreCaseAndDeletedAtIsNull(String code);
    boolean existsByCodeIgnoreCaseAndIdNotAndDeletedAtIsNull(String code, UUID id);

    @Query(value = """
            select distinct s.* from course_sections s
            join student_course_sections scs on scs.course_section_id = s.id
            where scs.student_id = :studentId and s.deleted_at is null
              and coalesce(scs.deleted_at, null) is null and coalesce(scs.is_active, 1) = 1
              and (:keyword is null or lower(coalesce(s.code, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(s.name, '')) like lower(concat('%', :keyword, '%')))
            order by s.code
            """, nativeQuery = true)
    List<CourseSection> findForStudent(@Param("studentId") UUID studentId, @Param("keyword") String keyword);

    @Query(value = """
            select distinct s.* from course_sections s
            join courses_classes cc on cc.course_id = s.course_id and cc.semester_id = s.semester_id
            join lecturer_course_classes lcc on lcc.course_class_id = cc.id
            where lcc.lecturer_id = :lecturerId and lcc.deleted_at is null
              and coalesce(lcc.is_active, 1) = 1 and s.deleted_at is null
              and coalesce(s.is_active, 1) = 1
              and (:keyword is null or lower(coalesce(s.code, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(s.name, '')) like lower(concat('%', :keyword, '%')))
            order by s.code
            """, nativeQuery = true)
    List<CourseSection> findForLecturer(@Param("lecturerId") UUID lecturerId, @Param("keyword") String keyword);
}
