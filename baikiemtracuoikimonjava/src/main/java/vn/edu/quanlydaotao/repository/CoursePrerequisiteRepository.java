package vn.edu.quanlydaotao.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.quanlydaotao.dto.CoursePrerequisiteEdge;
import vn.edu.quanlydaotao.entity.CoursePrerequisite;

public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisite, Long> {

	boolean existsByCourse_IdAndPrerequisiteCourse_Id(Long courseId, Long prerequisiteCourseId);

	@EntityGraph(attributePaths = "prerequisiteCourse")
	List<CoursePrerequisite> findByCourse_IdOrderByPrerequisiteCourse_CourseCode(Long courseId);

	Optional<CoursePrerequisite> findByCourse_IdAndPrerequisiteCourse_Id(Long courseId, Long prerequisiteCourseId);

	@Query("""
			select new vn.edu.quanlydaotao.dto.CoursePrerequisiteEdge(
				prerequisite.course.id, prerequisite.prerequisiteCourse.id)
			from CoursePrerequisite prerequisite
			""")
	List<CoursePrerequisiteEdge> findAllEdges();

	@Query("select distinct prerequisite.type from CoursePrerequisite prerequisite order by prerequisite.type")
	List<String> findDistinctTypes();
}
