package com.exam.service;

import com.exam.dto.ApiResponse;
import com.exam.dto.ExamRequestDto;
import com.exam.dto.ExamResponseDto;
import com.exam.dto.PaginationDto;
import com.exam.entity.Exam;
import com.exam.repository.ExamRegistrationRepository;
import com.exam.repository.ExamRepository;
import com.exam.repository.ExamRoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private static final UUID SYSTEM_ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final ExamRepository examRepository;
    private final ExamRoomRepository examRoomRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final InvigilatorService invigilatorService;

    public ExamService(
            ExamRepository examRepository,
            ExamRoomRepository examRoomRepository,
            ExamRegistrationRepository examRegistrationRepository,
            InvigilatorService invigilatorService) {
        this.examRepository = examRepository;
        this.examRoomRepository = examRoomRepository;
        this.examRegistrationRepository = examRegistrationRepository;
        this.invigilatorService = invigilatorService;
    }

    @Transactional
    public ApiResponse<Object> getAll(int page, int limit, String search, UUID semesterId, String examStatus, UUID examTypeId) {
        syncActiveExamStatuses();
        String querySearch = (search != null && !search.trim().isEmpty()) ? "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%" : null;
        String queryStatus = (examStatus != null && !examStatus.trim().isEmpty()) ? examStatus.trim() : null;

        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "examDate", "startTime"));
        Page<Exam> resultPage = examRepository.searchActive(querySearch, semesterId, queryStatus, examTypeId, pageRequest);

        List<ExamResponseDto> dtoList = resultPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        PaginationDto pagination = new PaginationDto(
                page, limit, resultPage.getTotalElements(), resultPage.getTotalPages() == 0 ? 1 : resultPage.getTotalPages()
        );
        return ApiResponse.successPage(dtoList, pagination);
    }

    public ApiResponse<Object> getById(UUID id) {
        Optional<Exam> opt = examRepository.findActiveById(id);
        if (opt.isPresent()) {
            return ApiResponse.successData(toDto(opt.get()));
        }
        return ApiResponse.error("Không tìm thấy lịch thi");
    }

    @Transactional
    public ApiResponse<Object> create(ExamRequestDto req) {
        if (req.getExamTypeId() == null || req.getCourseClassId() == null || req.getSemesterId() == null ||
            req.getExamDate() == null || req.getStartTime() == null || req.getDurationMinutes() == null) {
            return ApiResponse.error("Vui lòng điền đầy đủ các thông tin bắt buộc");
        }

        Exam entity = new Exam();
        entity.setExamTypeId(req.getExamTypeId());
        entity.setCourseClassId(req.getCourseClassId());
        entity.setSemesterId(req.getSemesterId());
        entity.setExamDate(LocalDate.parse(req.getExamDate()));
        
        LocalTime start = parseLocalTime(req.getStartTime());
        entity.setStartTime(start);
        entity.setDurationMinutes(req.getDurationMinutes());

        if (req.getEndTime() != null && !req.getEndTime().trim().isEmpty()) {
            entity.setEndTime(parseLocalTime(req.getEndTime()));
        } else {
            entity.setEndTime(start.plusMinutes(req.getDurationMinutes()));
        }

        entity.setExamFormat(req.getExamFormat() != null ? req.getExamFormat() : "thi máy");
        entity.setExamStatus(resolveAutoStatus(entity.getExamDate(), entity.getStartTime(), entity.getEndTime(), req.getExamStatus()));
        entity.setSupervisorCount(req.getSupervisorCount() != null ? req.getSupervisorCount() : 1);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(SYSTEM_ADMIN_ID);
        entity.setIsActive(true);

        if (req.getSupervisorCount() != null && (req.getSupervisorCount() < 1 || req.getSupervisorCount() > 10)) {
            return ApiResponse.error("Số lượng cán bộ coi thi phải từ 1 đến 10");
        }
        // Các thao tác JPA và JDBC dùng chung transaction, không lưu phân công dở dang.
        Exam saved = examRepository.saveAndFlush(entity);
        invigilatorService.saveAssignments(saved.getId(), req.getInvigilatorIds(), saved.getSupervisorCount());
        return ApiResponse.success("Thêm lịch thi thành công", toDto(saved));
    }

    @Transactional
    public ApiResponse<Object> update(UUID id, ExamRequestDto req) {
        Optional<Exam> opt = examRepository.findActiveById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy lịch thi để cập nhật");
        }

        Exam existing = opt.get();
        if (req.getExamTypeId() != null) existing.setExamTypeId(req.getExamTypeId());
        if (req.getCourseClassId() != null) existing.setCourseClassId(req.getCourseClassId());
        if (req.getSemesterId() != null) existing.setSemesterId(req.getSemesterId());
        if (req.getExamDate() != null) existing.setExamDate(LocalDate.parse(req.getExamDate()));
        if (req.getStartTime() != null) {
            LocalTime start = parseLocalTime(req.getStartTime());
            existing.setStartTime(start);
            if (req.getDurationMinutes() != null) {
                existing.setDurationMinutes(req.getDurationMinutes());
                existing.setEndTime(start.plusMinutes(req.getDurationMinutes()));
            }
        } else if (req.getDurationMinutes() != null) {
            existing.setDurationMinutes(req.getDurationMinutes());
            existing.setEndTime(existing.getStartTime().plusMinutes(req.getDurationMinutes()));
        }

        if (req.getEndTime() != null && !req.getEndTime().trim().isEmpty()) {
            existing.setEndTime(parseLocalTime(req.getEndTime()));
        }

        if (req.getExamFormat() != null) existing.setExamFormat(req.getExamFormat());
        String requestedStatus = req.getExamStatus() != null ? req.getExamStatus() : existing.getExamStatus();
        existing.setExamStatus(resolveAutoStatus(existing.getExamDate(), existing.getStartTime(), existing.getEndTime(), requestedStatus));
        if (req.getSupervisorCount() != null) existing.setSupervisorCount(req.getSupervisorCount());

        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(SYSTEM_ADMIN_ID);

        if (existing.getSupervisorCount() == null || existing.getSupervisorCount() < 1 || existing.getSupervisorCount() > 10) {
            return ApiResponse.error("Số lượng cán bộ coi thi phải từ 1 đến 10");
        }
        Exam saved = examRepository.saveAndFlush(existing);
        invigilatorService.saveAssignments(saved.getId(), req.getInvigilatorIds(), saved.getSupervisorCount());
        return ApiResponse.success("Cập nhật lịch thi thành công", toDto(saved));
    }

    @Transactional
    public ApiResponse<Object> softDelete(UUID id) {
        Optional<Exam> opt = examRepository.findActiveById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy lịch thi");
        }
        long regCount = examRegistrationRepository.countByExamIdAndIsActiveTrueAndDeletedAtIsNull(id);
        if (regCount > 0) {
            return ApiResponse.error("Không thể xóa lịch thi vì đã có " + regCount + " thí sinh đăng ký!");
        }
        Exam existing = opt.get();
        existing.setIsActive(false);
        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(SYSTEM_ADMIN_ID);
        examRepository.save(existing);
        return ApiResponse.successMessage("Xóa mềm lịch thi thành công");
    }

    private void syncActiveExamStatuses() {
        List<Exam> activeExams = examRepository.findAllActive();
        for (Exam exam : activeExams) {
            String resolved = resolveAutoStatus(exam.getExamDate(), exam.getStartTime(), exam.getEndTime(), exam.getExamStatus());
            if (!resolved.equals(exam.getExamStatus())) {
                exam.setExamStatus(resolved);
                examRepository.save(exam);
            }
        }
    }

    private String resolveAutoStatus(LocalDate examDate, LocalTime startTime, LocalTime endTime, String currentStatus) {
        if (currentStatus != null) {
            String normalized = currentStatus.trim().toLowerCase(java.util.Locale.ROOT);
            if (normalized.equals("hoãn") || normalized.equals("tạm hoãn") || normalized.equals("đã hủy") || normalized.equals("hủy")) {
                return currentStatus.trim();
            }
        }
        if (examDate == null || startTime == null || endTime == null) {
            return currentStatus != null ? currentStatus.trim() : "Đã lên lịch";
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDt = LocalDateTime.of(examDate, startTime);
        LocalDateTime endDt = LocalDateTime.of(examDate, endTime);
        if (now.isBefore(startDt)) {
            return "Đã lên lịch";
        }
        if (!now.isAfter(endDt)) {
            return "Đang diễn ra";
        }
        return "Kết thúc";
    }

    private LocalTime parseLocalTime(String timeStr) {
        String clean = timeStr.trim();
        if (clean.length() == 5) {
            return LocalTime.parse(clean, DateTimeFormatter.ofPattern("HH:mm"));
        }
        return LocalTime.parse(clean);
    }

    private ExamResponseDto toDto(Exam exam) {
        ExamResponseDto dto = new ExamResponseDto();
        dto.setId(exam.getId());
        dto.setExamTypeId(exam.getExamTypeId());
        if (exam.getExamType() != null) {
            dto.setExamTypeName(exam.getExamType().getName());
        }

        dto.setCourseClassId(exam.getCourseClassId());
        if (exam.getCourse() != null) {
            dto.setCourseCode(exam.getCourse().getCourseCode());
            dto.setCourseName(exam.getCourse().getCourseName());
        }

        dto.setSemesterId(exam.getSemesterId());
        if (exam.getSemester() != null) {
            dto.setSemesterName(exam.getSemester().getSemesterName());
            dto.setAcademicYear(exam.getSemester().getAcademicYear());
        }

        if (exam.getExamDate() != null) {
            dto.setExamDate(exam.getExamDate().toString());
        }
        if (exam.getStartTime() != null) {
            dto.setStartTime(exam.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        dto.setDurationMinutes(exam.getDurationMinutes());
        if (exam.getEndTime() != null) {
            dto.setEndTime(exam.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        dto.setExamFormat(exam.getExamFormat());
        dto.setExamStatus(exam.getExamStatus());
        dto.setSupervisorCount(exam.getSupervisorCount());
        var invigilators = invigilatorService.listAssigned(exam.getId());
        dto.setInvigilators(invigilators);
        dto.setInvigilatorIds(invigilators.stream()
                .map(item -> UUID.fromString((String) item.get("id"))).collect(Collectors.toList()));

        dto.setRoomCount(examRoomRepository.countByExamIdAndIsActiveTrueAndDeletedAtIsNull(exam.getId()));
        dto.setRegistrationCount(examRegistrationRepository.countByExamIdAndIsActiveTrueAndDeletedAtIsNull(exam.getId()));
        return dto;
    }
}
