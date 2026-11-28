package com.exam.service;

import com.exam.dto.ApiResponse;
import com.exam.dto.PaginationDto;
import com.exam.entity.ExamType;
import com.exam.repository.ExamRepository;
import com.exam.repository.ExamTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExamTypeService {

    private static final UUID SYSTEM_ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final ExamTypeRepository examTypeRepository;
    private final ExamRepository examRepository;

    public ExamTypeService(ExamTypeRepository examTypeRepository, ExamRepository examRepository) {
        this.examTypeRepository = examTypeRepository;
        this.examRepository = examRepository;
    }

    public ApiResponse<Object> getAll(int page, int limit, String search) {
        String querySearch = (search != null && !search.trim().isEmpty()) ? "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%" : null;
        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ExamType> resultPage = examTypeRepository.searchActive(querySearch, pageRequest);

        PaginationDto pagination = new PaginationDto(
                page, limit, resultPage.getTotalElements(), resultPage.getTotalPages() == 0 ? 1 : resultPage.getTotalPages()
        );
        return ApiResponse.successPage(resultPage.getContent(), pagination);
    }

    public ApiResponse<Object> getById(UUID id) {
        Optional<ExamType> opt = examTypeRepository.findActiveById(id);
        if (opt.isPresent()) {
            return ApiResponse.successData(opt.get());
        }
        return ApiResponse.error("Không tìm thấy loại kỳ thi");
    }

    @Transactional
    public ApiResponse<Object> create(ExamType request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ApiResponse.error("Tên loại kỳ thi là bắt buộc");
        }
        ExamType entity = new ExamType();
        entity.setName(request.getName().trim());
        entity.setDescription(request.getDescription());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(SYSTEM_ADMIN_ID);
        entity.setIsActive(true);

        ExamType saved = examTypeRepository.save(entity);
        return ApiResponse.success("Thêm mới loại kỳ thi thành công", saved);
    }

    @Transactional
    public ApiResponse<Object> update(UUID id, ExamType request) {
        Optional<ExamType> opt = examTypeRepository.findActiveById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy loại kỳ thi để cập nhật");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ApiResponse.error("Tên loại kỳ thi không được để trống");
        }
        ExamType existing = opt.get();
        existing.setName(request.getName().trim());
        existing.setDescription(request.getDescription());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(SYSTEM_ADMIN_ID);

        ExamType saved = examTypeRepository.save(existing);
        return ApiResponse.success("Cập nhật loại kỳ thi thành công", saved);
    }

    @Transactional
    public ApiResponse<Object> softDelete(UUID id) {
        Optional<ExamType> opt = examTypeRepository.findActiveById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy loại kỳ thi");
        }
        long refCount = examRepository.countByExamTypeIdAndIsActiveTrueAndDeletedAtIsNull(id);
        if (refCount > 0) {
            return ApiResponse.error("Không thể xóa loại kỳ thi này vì đang có " + refCount + " lịch thi sử dụng!");
        }
        ExamType existing = opt.get();
        existing.setIsActive(false);
        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(SYSTEM_ADMIN_ID);
        examTypeRepository.save(existing);
        return ApiResponse.successMessage("Xóa mềm loại kỳ thi thành công");
    }
}
