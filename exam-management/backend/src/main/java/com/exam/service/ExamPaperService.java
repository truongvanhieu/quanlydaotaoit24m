package com.exam.service;

import com.exam.dto.ApiResponse;
import com.exam.dto.ExamPaperRequestDto;
import com.exam.dto.ExamPaperResponseDto;
import com.exam.dto.PaginationDto;
import com.exam.entity.ExamPaper;
import com.exam.repository.ExamPaperRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExamPaperService {

    private static final UUID SYSTEM_ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final ExamPaperRepository examPaperRepository;

    public ExamPaperService(ExamPaperRepository examPaperRepository) {
        this.examPaperRepository = examPaperRepository;
    }

    public ApiResponse<Object> getAll(int page, int limit, String search, UUID examId) {
        String querySearch = (search != null && !search.trim().isEmpty()) ? "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%" : null;

        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ExamPaper> resultPage = examPaperRepository.searchActive(querySearch, examId, pageRequest);

        List<ExamPaperResponseDto> dtoList = resultPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        PaginationDto pagination = new PaginationDto(
                page, limit, resultPage.getTotalElements(), resultPage.getTotalPages() == 0 ? 1 : resultPage.getTotalPages()
        );
        return ApiResponse.successPage(dtoList, pagination);
    }

    public ApiResponse<Object> getById(UUID id) {
        Optional<ExamPaper> opt = examPaperRepository.findActiveById(id);
        if (opt.isPresent()) {
            return ApiResponse.successData(toDto(opt.get()));
        }
        return ApiResponse.error("Không tìm thấy đề thi");
    }

    @Transactional
    public ApiResponse<Object> create(ExamPaperRequestDto req) {
        if (req.getExamId() == null || req.getPaperCode() == null || req.getFileUrl() == null ||
            req.getPaperCode().trim().isEmpty() || req.getFileUrl().trim().isEmpty()) {
            return ApiResponse.error("Vui lòng điền đủ kỳ thi, mã đề thi và đường dẫn file đề");
        }

        ExamPaper entity = new ExamPaper();
        entity.setExamId(req.getExamId());
        entity.setPaperCode(req.getPaperCode().trim());
        entity.setFileUrl(req.getFileUrl().trim());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(SYSTEM_ADMIN_ID);
        entity.setIsActive(true);

        ExamPaper saved = examPaperRepository.save(entity);
        return ApiResponse.success("Thêm đề thi thành công", toDto(saved));
    }

    @Transactional
    public ApiResponse<Object> update(UUID id, ExamPaperRequestDto req) {
        Optional<ExamPaper> opt = examPaperRepository.findActiveById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi để cập nhật");
        }
        ExamPaper existing = opt.get();
        if (req.getExamId() != null) existing.setExamId(req.getExamId());
        if (req.getPaperCode() != null && !req.getPaperCode().trim().isEmpty()) {
            existing.setPaperCode(req.getPaperCode().trim());
        }
        if (req.getFileUrl() != null && !req.getFileUrl().trim().isEmpty()) {
            existing.setFileUrl(req.getFileUrl().trim());
        }

        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(SYSTEM_ADMIN_ID);

        ExamPaper saved = examPaperRepository.save(existing);
        return ApiResponse.success("Cập nhật đề thi thành công", toDto(saved));
    }

    @Transactional
    public ApiResponse<Object> softDelete(UUID id) {
        Optional<ExamPaper> opt = examPaperRepository.findActiveById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi");
        }
        ExamPaper existing = opt.get();
        existing.setIsActive(false);
        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(SYSTEM_ADMIN_ID);
        examPaperRepository.save(existing);
        return ApiResponse.successMessage("Xóa mềm đề thi thành công");
    }

    private ExamPaperResponseDto toDto(ExamPaper ep) {
        ExamPaperResponseDto dto = new ExamPaperResponseDto();
        dto.setId(ep.getId());
        dto.setExamId(ep.getExamId());
        if (ep.getExam() != null) {
            if (ep.getExam().getCourse() != null) {
                dto.setCourseCode(ep.getExam().getCourse().getCourseCode());
                dto.setCourseName(ep.getExam().getCourse().getCourseName());
            }
            if (ep.getExam().getExamDate() != null) {
                dto.setExamDate(ep.getExam().getExamDate().toString());
            }
            if (ep.getExam().getStartTime() != null) {
                dto.setStartTime(ep.getExam().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }
        dto.setPaperCode(ep.getPaperCode());
        dto.setFileUrl(ep.getFileUrl());
        if (ep.getCreatedAt() != null) {
            dto.setCreatedAt(ep.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return dto;
    }
}
