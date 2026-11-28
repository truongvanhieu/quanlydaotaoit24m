package com.exam.service;

import com.exam.dto.ApiResponse;
import com.exam.dto.ExamResultRequestDto;
import com.exam.dto.ExamResultResponseDto;
import com.exam.dto.PaginationDto;
import com.exam.entity.ExamRegistration;
import com.exam.entity.ExamResult;
import com.exam.repository.ExamRegistrationRepository;
import com.exam.repository.ExamResultRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExamResultService {

    private static final UUID SYSTEM_ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final BigDecimal PASS_SCORE = new BigDecimal("4.00");

    private static boolean isSpecialStatus(String status) {
        return "Vắng".equals(status) || "Vi phạm".equals(status);
    }

    private static String statusForScore(BigDecimal score) {
        return score.compareTo(PASS_SCORE) >= 0 ? "Đạt" : "Không đạt";
    }

    private final ExamResultRepository examResultRepository;
    private final ExamRegistrationRepository examRegistrationRepository;

    public ExamResultService(
            ExamResultRepository examResultRepository,
            ExamRegistrationRepository examRegistrationRepository) {
        this.examResultRepository = examResultRepository;
        this.examRegistrationRepository = examRegistrationRepository;
    }

    public ApiResponse<Object> getAll(int page, int limit, String search, UUID examId, String status, Boolean isLocked, String appealStatus) {
        String querySearch = (search != null && !search.trim().isEmpty()) ? "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%" : null;
        String queryStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        String queryAppeal = (appealStatus != null && !appealStatus.trim().isEmpty()) ? appealStatus.trim() : null;

        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ExamResult> resultPage = examResultRepository.searchActive(querySearch, examId, queryStatus, isLocked, queryAppeal, pageRequest);

        List<ExamResultResponseDto> dtoList = resultPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        PaginationDto pagination = new PaginationDto(
                page, limit, resultPage.getTotalElements(), resultPage.getTotalPages() == 0 ? 1 : resultPage.getTotalPages()
        );
        return ApiResponse.successPage(dtoList, pagination);
    }

    public ApiResponse<Object> getById(UUID id) {
        Optional<ExamResult> opt = examResultRepository.findActiveById(id);
        if (opt.isPresent()) {
            return ApiResponse.successData(toDto(opt.get()));
        }
        return ApiResponse.error("Không tìm thấy kết quả thi");
    }

    @Transactional
    public ApiResponse<Object> create(ExamResultRequestDto req) {
        if (req.getRegistrationId() == null) {
            return ApiResponse.error("Vui lòng chọn thí sinh đăng ký");
        }

        Optional<ExamResult> existingOpt = examResultRepository.findByRegistrationIdActive(req.getRegistrationId());
        if (existingOpt.isPresent()) {
            return ApiResponse.error("Thí sinh này đã có kết quả thi trong hệ thống. Vui lòng sử dụng chức năng cập nhật điểm!");
        }

        if (req.getScore() != null) {
            if (req.getScore().compareTo(BigDecimal.ZERO) < 0 || req.getScore().compareTo(BigDecimal.valueOf(10)) > 0) {
                return ApiResponse.error("Điểm số phải nằm trong khoảng từ 0.00 đến 10.00");
            }
        }

        BigDecimal score = req.getScore() == null ? null : req.getScore().setScale(2, RoundingMode.HALF_UP);
        String finalStatus = req.getStatus() == null ? null : req.getStatus().trim();
        if (!isSpecialStatus(finalStatus)) {
            if (score == null) return ApiResponse.error("Vui lòng nhập điểm hoặc chọn Vắng / Vi phạm");
            finalStatus = statusForScore(score);
        }

        ExamResult entity = new ExamResult();
        entity.setRegistrationId(req.getRegistrationId());
        entity.setScore(score);
        entity.setStatus(finalStatus);
        entity.setGradedBy(SYSTEM_ADMIN_ID);
        entity.setGradedAt(LocalDateTime.now());
        entity.setIsLocked(false);
        entity.setAppealStatus(req.getAppealStatus() != null ? req.getAppealStatus() : "Không");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(SYSTEM_ADMIN_ID);
        entity.setIsActive(true);

        ExamResult saved = examResultRepository.save(entity);
        return ApiResponse.success("Nhập điểm thành công", toDto(saved));
    }

    @Transactional
    public ApiResponse<Object> update(UUID id, ExamResultRequestDto req) {
        Optional<ExamResult> existingOpt = examResultRepository.findActiveById(id);
        if (existingOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy kết quả thi");
        }

        ExamResult existing = existingOpt.get();

        // QUY TẮC CỐT LÕI: NẾU is_locked = 1 THÌ BACKEND BẮT BUỘC TỪ CHỐI SỬA ĐIỂM
        if (Boolean.TRUE.equals(existing.getIsLocked())) {
            return ApiResponse.error("KẾT QUẢ THI ĐÃ BỊ KHÓA (CHỐT ĐIỂM)! Backend từ chối mọi yêu cầu chỉnh sửa điểm khi is_locked = 1.");
        }

        if (req.getScore() != null) {
            if (req.getScore().compareTo(BigDecimal.ZERO) < 0 || req.getScore().compareTo(BigDecimal.valueOf(10)) > 0) {
                return ApiResponse.error("Điểm số phải nằm trong khoảng từ 0.00 đến 10.00");
            }
            existing.setScore(req.getScore().setScale(2, RoundingMode.HALF_UP));
        }

        String requestedStatus = req.getStatus() == null ? null : req.getStatus().trim();
        if (isSpecialStatus(requestedStatus)) {
            existing.setStatus(requestedStatus);
        } else if (req.getScore() != null || (requestedStatus != null && !requestedStatus.isEmpty())) {
            if (existing.getScore() != null) {
                existing.setStatus(statusForScore(existing.getScore()));
            }
        }

        if (req.getAppealStatus() != null && !req.getAppealStatus().trim().isEmpty()) {
            existing.setAppealStatus(req.getAppealStatus().trim());
        }

        existing.setGradedBy(SYSTEM_ADMIN_ID);
        existing.setGradedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(SYSTEM_ADMIN_ID);

        ExamResult saved = examResultRepository.save(existing);
        return ApiResponse.success("Cập nhật điểm thi thành công", toDto(saved));
    }

    @Transactional
    public ApiResponse<Object> toggleLock(UUID id, Boolean targetLockState) {
        Optional<ExamResult> opt = examResultRepository.findActiveById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy kết quả thi");
        }
        ExamResult existing = opt.get();
        boolean newState = (targetLockState != null) ? targetLockState : !Boolean.TRUE.equals(existing.getIsLocked());
        existing.setIsLocked(newState);
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(SYSTEM_ADMIN_ID);

        examResultRepository.save(existing);
        String message = newState ? "Đã chốt điểm thành công! (Điểm hiện đã bị khóa)" : "Đã mở khóa điểm thi!";
        return ApiResponse.success(message, toDto(existing));
    }

    @Transactional
    public ApiResponse<Object> updateAppeal(UUID id, ExamResultRequestDto req) {
        Optional<ExamResult> opt = examResultRepository.findActiveById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy kết quả thi");
        }
        if (req.getAppealStatus() == null || req.getAppealStatus().trim().isEmpty()) {
            return ApiResponse.error("Vui lòng cung cấp trạng thái phúc khảo");
        }

        ExamResult existing = opt.get();
        if (req.getNewScore() != null) {
            if (req.getNewScore().compareTo(BigDecimal.ZERO) < 0 || req.getNewScore().compareTo(BigDecimal.valueOf(10)) > 0) {
                return ApiResponse.error("Điểm phúc khảo phải từ 0.00 đến 10.00");
            }
            BigDecimal newScore = req.getNewScore().setScale(2, RoundingMode.HALF_UP);
            boolean scoreChanged = existing.getScore() == null || existing.getScore().compareTo(newScore) != 0;
            existing.setScore(newScore);
            if (scoreChanged || !isSpecialStatus(existing.getStatus())) {
                existing.setStatus(statusForScore(newScore));
            }
        }
        existing.setAppealStatus(req.getAppealStatus().trim());

        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(SYSTEM_ADMIN_ID);

        examResultRepository.save(existing);
        return ApiResponse.success("Cập nhật tình trạng phúc khảo thành công", toDto(existing));
    }

    @Transactional
    public ApiResponse<Object> softDelete(UUID id) {
        Optional<ExamResult> opt = examResultRepository.findActiveById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy kết quả thi");
        }
        ExamResult existing = opt.get();
        if (Boolean.TRUE.equals(existing.getIsLocked())) {
            return ApiResponse.error("Điểm thi này đã bị chốt (is_locked = 1). Không thể xóa kết quả đã chốt!");
        }
        existing.setIsActive(false);
        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(SYSTEM_ADMIN_ID);
        examResultRepository.save(existing);
        return ApiResponse.successMessage("Xóa mềm kết quả thi thành công");
    }

    private ExamResultResponseDto toDto(ExamResult res) {
        ExamResultResponseDto dto = new ExamResultResponseDto();
        dto.setId(res.getId());
        dto.setRegistrationId(res.getRegistrationId());
        if (res.getRegistration() != null) {
            dto.setRollNumber(res.getRegistration().getRollNumber());
            dto.setExamId(res.getRegistration().getExamId());
            dto.setStudentId(res.getRegistration().getStudentId());

            if (res.getRegistration().getStudent() != null) {
                dto.setStudentCode(res.getRegistration().getStudent().getStudentCode());
                dto.setStudentName(res.getRegistration().getStudent().getFullName());
                dto.setStudentClass(res.getRegistration().getStudent().getClassName());
            }

            if (res.getRegistration().getExam() != null && res.getRegistration().getExam().getCourse() != null) {
                dto.setCourseCode(res.getRegistration().getExam().getCourse().getCourseCode());
                dto.setCourseName(res.getRegistration().getExam().getCourse().getCourseName());
            }

            if (res.getRegistration().getExamRoom() != null && res.getRegistration().getExamRoom().getRoom() != null) {
                dto.setRoomNumber(res.getRegistration().getExamRoom().getRoom().getRoomNumber());
            }
        }

        dto.setScore(res.getScore());
        dto.setStatus(res.getStatus());
        dto.setGradedBy(res.getGradedBy());
        if (res.getGradedAt() != null) {
            dto.setGradedAt(res.getGradedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        dto.setIsLocked(res.getIsLocked());
        dto.setAppealStatus(res.getAppealStatus());
        return dto;
    }
}
