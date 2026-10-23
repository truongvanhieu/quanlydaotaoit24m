package vn.edu.quanlydaotao.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.quanlydaotao.dto.MajorRequest;
import vn.edu.quanlydaotao.dto.MajorResponse;
import vn.edu.quanlydaotao.dto.PagedResponse;
import vn.edu.quanlydaotao.entity.Major;
import vn.edu.quanlydaotao.exception.ConflictException;
import vn.edu.quanlydaotao.exception.NotFoundException;
import vn.edu.quanlydaotao.repository.MajorRepository;

@Service
@Transactional(readOnly = true)
public class MajorService {

	private final MajorRepository majorRepository;

	public MajorService(MajorRepository majorRepository) {
		this.majorRepository = majorRepository;
	}

	public PagedResponse<MajorResponse> getMajors(String query, int page, int limit) {
		String keyword = query == null || query.isBlank() ? null : query.trim();
		PageRequest pageable = PageRequest.of(
				page,
				limit,
				Sort.by(Sort.Direction.ASC, "majorCode"));
		Page<MajorResponse> result = majorRepository.search(keyword, pageable)
				.map(MajorResponse::from);
		return PagedResponse.from(result);
	}

	public MajorResponse getMajor(Long id) {
		return MajorResponse.from(findMajor(id));
	}

	@Transactional
	public MajorResponse createMajor(MajorRequest request) {
		if (majorRepository.existsByMajorCodeIgnoreCase(request.majorCode().trim())) {
			throw new ConflictException("Mã ngành đã tồn tại.");
		}

		Major major = new Major();
		applyRequest(major, request, true);
		return MajorResponse.from(majorRepository.save(major));
	}

	@Transactional
	public MajorResponse updateMajor(Long id, MajorRequest request) {
		Major major = findMajor(id);
		String majorCode = request.majorCode().trim();
		if (!major.getMajorCode().equalsIgnoreCase(majorCode)
				&& majorRepository.existsByMajorCodeIgnoreCaseAndIdNot(majorCode, id)) {
			throw new ConflictException("Mã ngành đã tồn tại.");
		}

		applyRequest(major, request, false);
		return MajorResponse.from(majorRepository.save(major));
	}

	@Transactional
	public MajorResponse deactivateMajor(Long id) {
		Major major = findMajor(id);
		major.setActive(false);
		return MajorResponse.from(majorRepository.save(major));
	}

	private Major findMajor(Long id) {
		return majorRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Không tìm thấy ngành đào tạo với ID " + id + "."));
	}

	private void applyRequest(Major major, MajorRequest request, boolean creating) {
		major.setMajorCode(request.majorCode().trim());
		major.setMajorName(request.majorName().trim());
		major.setDescription(request.description());
		if (request.isActive() != null) {
			major.setActive(request.isActive());
		} else if (creating) {
			major.setActive(true);
		}
	}
}
