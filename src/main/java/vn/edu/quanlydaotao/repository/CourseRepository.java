package vn.edu.quanlydaotao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.quanlydaotao.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

	boolean existsByCourseCodeIgnoreCase(String courseCode);

	boolean existsByCourseCodeIgnoreCaseAndIdNot(String courseCode, Long id);

	@Query("""
			select c from Course c
			where (:keyword is null
				or lower(c.courseCode) like lower(concat('%', :keyword, '%'))
				or lower(c.courseName) like lower(concat('%', :keyword, '%')))
				and (:credits is null or c.credits = :credits)
			""")
	Page<Course> search(
			@Param("keyword") String keyword,
			@Param("credits") Integer credits,
			Pageable pageable);
}
