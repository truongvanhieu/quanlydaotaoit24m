package vn.edu.quanlydaotao.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.quanlydaotao.entity.ProgramCourse;

public interface ProgramCourseRepository extends JpaRepository<ProgramCourse, Long> {

	boolean existsByProgram_IdAndCourse_Id(Long programId, Long courseId);

	Optional<ProgramCourse> findByProgram_IdAndCourse_Id(Long programId, Long courseId);

	void deleteByProgram_IdAndCourse_Id(Long programId, Long courseId);

	@Query("select coalesce(sum(pc.course.credits), 0) from ProgramCourse pc where pc.program.id = :programId")
	Long sumCourseCreditsByProgramId(@Param("programId") Long programId);
}
