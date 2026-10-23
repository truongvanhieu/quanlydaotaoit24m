package vn.edu.quanlydaotao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.quanlydaotao.entity.Major;

public interface MajorRepository extends JpaRepository<Major, Long> {

	boolean existsByMajorCodeIgnoreCase(String majorCode);

	boolean existsByMajorCodeIgnoreCaseAndIdNot(String majorCode, Long id);

	@Query("""
			select m from Major m
			where (:keyword is null
				or lower(m.majorCode) like lower(concat('%', :keyword, '%'))
				or lower(m.majorName) like lower(concat('%', :keyword, '%')))
			""")
	Page<Major> search(@Param("keyword") String keyword, Pageable pageable);
}
