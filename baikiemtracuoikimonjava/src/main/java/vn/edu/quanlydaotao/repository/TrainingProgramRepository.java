package vn.edu.quanlydaotao.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.quanlydaotao.entity.TrainingProgram;

public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, Long> {

	boolean existsByProgramCodeIgnoreCase(String programCode);

	boolean existsByProgramCodeIgnoreCaseAndIdNot(String programCode, Long id);

	@EntityGraph(attributePaths = "major")
	@Query("""
			select p from TrainingProgram p
			where (:majorId is null or p.major.id = :majorId)
				and (:cohort is null or p.cohort = :cohort)
				and (:academicYear is null or p.academicYear = :academicYear)
				and (:keyword is null
					or lower(p.programCode) like lower(concat('%', :keyword, '%'))
					or lower(p.programName) like lower(concat('%', :keyword, '%')))
			""")
	Page<TrainingProgram> search(
			@Param("majorId") Long majorId,
			@Param("cohort") String cohort,
			@Param("academicYear") String academicYear,
			@Param("keyword") String keyword,
			Pageable pageable);

	@EntityGraph(attributePaths = {"major", "programCourses", "programCourses.course"})
	@Query("select distinct p from TrainingProgram p where p.id = :id")
	Optional<TrainingProgram> findDetailedById(@Param("id") Long id);
}
