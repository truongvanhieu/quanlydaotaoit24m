package com.exam.service;

import com.exam.dto.ApiResponse;
import com.exam.dto.ExamRoomRequestDto;
import com.exam.dto.ExamRoomResponseDto;
import com.exam.dto.PaginationDto;
import com.exam.entity.Exam;
import com.exam.entity.ExamRoom;
import com.exam.entity.Room;
import com.exam.repository.ExamRegistrationRepository;
import com.exam.repository.ExamRepository;
import com.exam.repository.ExamRoomRepository;
import com.exam.repository.RoomRepository;
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
public class ExamRoomService {

    private static final UUID SYSTEM_ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final ExamRoomRepository examRoomRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final RoomRepository roomRepository;
    private final ExamRepository examRepository;

    public ExamRoomService(
            ExamRoomRepository examRoomRepository,
            ExamRegistrationRepository examRegistrationRepository,
            RoomRepository roomRepository,
            ExamRepository examRepository) {
        this.examRoomRepository = examRoomRepository;
        this.examRegistrationRepository = examRegistrationRepository;
        this.roomRepository = roomRepository;
        this.examRepository = examRepository;
    }

    public ApiResponse<Object> getAll(int page, int limit, String search, UUID examId) {
        String querySearch = (search != null && !search.trim().isEmpty()) ? "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%" : null;

        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ExamRoom> resultPage = examRoomRepository.searchActive(querySearch, examId, pageRequest);

        List<ExamRoomResponseDto> dtoList = resultPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        PaginationDto pagination = new PaginationDto(
                page, limit, resultPage.getTotalElements(), resultPage.getTotalPages() == 0 ? 1 : resultPage.getTotalPages()
        );
        return ApiResponse.successPage(dtoList, pagination);
    }

    public ApiResponse<Object> getById(UUID id) {
        Optional<ExamRoom> opt = examRoomRepository.findActiveById(id);
        if (opt.isPresent()) {
            return ApiResponse.successData(toDto(opt.get()));
        }
        return ApiResponse.error("Không tìm thấy phòng thi");
    }

    @Transactional
    public ApiResponse<Object> create(ExamRoomRequestDto req) {
        if (req.getExamId() == null || req.getRoomId() == null || req.getCapacity() == null) {
            return ApiResponse.error("Vui lòng chọn kỳ thi, phòng và nhập sức chứa (capacity)");
        }

        if (req.getCapacity() <= 0) {
            return ApiResponse.error("Sức chứa phòng thi phải là số nguyên dương lớn hơn 0");
        }

        Optional<Room> physicalRoomOpt = roomRepository.findById(req.getRoomId());
        if (physicalRoomOpt.isEmpty()) {
            return ApiResponse.error("Phòng học/thi vật lý không tồn tại");
        }

        Room physicalRoom = physicalRoomOpt.get();
        if (req.getCapacity() > physicalRoom.getCapacity()) {
            return ApiResponse.error("Sức chứa cấu hình (" + req.getCapacity() +
                    ") vượt quá sức chứa thiết kế của phòng " + physicalRoom.getRoomNumber() +
                    " (" + physicalRoom.getCapacity() + " chỗ)!");
        }

        List<ExamRoom> duplicates = examRoomRepository.findByExamIdAndRoomIdActive(req.getExamId(), req.getRoomId());
        if (!duplicates.isEmpty()) {
            return ApiResponse.error("Phòng này đã được gán cho ca thi này rồi!");
        }

        ExamRoom entity = new ExamRoom();
        entity.setExamId(req.getExamId());
        entity.setRoomId(req.getRoomId());
        entity.setCapacity(req.getCapacity());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(SYSTEM_ADMIN_ID);
        entity.setIsActive(true);

        ExamRoom saved = examRoomRepository.save(entity);
        return ApiResponse.success("Gán phòng thi thành công", toDto(saved));
    }

    /**
     * Sửa đầy đủ thông tin gán phòng và phòng vật lý trong cùng giao dịch.
     * Các bản ghi đăng ký sẽ không bị di chuyển sang ca/phòng khác.
     */
    @Transactional
    public ApiResponse<Object> update(UUID id, ExamRoomRequestDto req) {
        Optional<ExamRoom> opt = examRoomRepository.findActiveById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy phòng thi");
        }

        ExamRoom existing = opt.get();
        UUID nextExamId = req.getExamId() != null ? req.getExamId() : existing.getExamId();
        UUID nextRoomId = req.getRoomId() != null ? req.getRoomId() : existing.getRoomId();
        int nextCapacity = req.getCapacity() != null ? req.getCapacity() : existing.getCapacity();

        Optional<Exam> examOpt = examRepository.findActiveById(nextExamId);
        Optional<Room> roomOpt = roomRepository.findById(nextRoomId);
        if (examOpt.isEmpty() || roomOpt.isEmpty() || !Boolean.TRUE.equals(roomOpt.get().getIsActive())) {
            return ApiResponse.error("Ca thi hoặc phòng vật lý không tồn tại / đã ngừng hoạt động");
        }
        Room physicalRoom = roomOpt.get();
        long registered = examRegistrationRepository.countByExamRoomIdAndIsActiveTrueAndDeletedAtIsNull(id);
        if (registered > 0 && (!nextExamId.equals(existing.getExamId()) || !nextRoomId.equals(existing.getRoomId()))) {
            return ApiResponse.error("Phòng đã có " + registered +
                    " thí sinh. Hãy chuyển/hủy đăng ký trước khi đổi ca thi hoặc phòng vật lý.");
        }
        if (nextCapacity <= 0 || nextCapacity < registered) {
            return ApiResponse.error("Sức chứa ca thi phải lớn hơn 0 và không nhỏ hơn " + registered + " thí sinh đang đăng ký");
        }
        boolean duplicate = examRoomRepository.findByExamIdAndRoomIdActive(nextExamId, nextRoomId)
                .stream().anyMatch(other -> !other.getId().equals(id));
        if (duplicate) {
            return ApiResponse.error("Phòng này đã được gán cho ca thi đã chọn");
        }

        // Các thông tin phòng vật lý áp dụng cho tất cả ca thi đang dùng cùng phòng.
        String nextNumber = req.getRoomNumber() == null ? physicalRoom.getRoomNumber() : req.getRoomNumber().trim();
        String nextBuilding = req.getBuilding() == null ? physicalRoom.getBuilding() : req.getBuilding().trim();
        int nextMax = req.getRoomMaxCapacity() == null ? physicalRoom.getCapacity() : req.getRoomMaxCapacity();
        if (nextNumber.isEmpty() || nextNumber.length() > 50 || nextBuilding.isEmpty()
                || nextBuilding.length() > 100 || nextMax <= 0 || nextMax > 10000) {
            return ApiResponse.error("Mã phòng (1–50 ký tự), địa điểm (1–100 ký tự) hoặc sức chứa thiết kế không hợp lệ");
        }
        if (nextCapacity > nextMax) {
            return ApiResponse.error("Sức chứa ca thi không được vượt quá sức chứa thiết kế (" + nextMax + ")");
        }
        boolean roomCodeTaken = roomRepository.findAll().stream()
                .anyMatch(r -> !r.getId().equals(nextRoomId) && r.getRoomNumber().equalsIgnoreCase(nextNumber));
        if (roomCodeTaken) {
            return ApiResponse.error("Mã phòng '" + nextNumber + "' đã tồn tại");
        }
        boolean otherAssignmentsExceedDesign = examRoomRepository.findAllActive().stream()
                .anyMatch(er -> er.getRoomId().equals(nextRoomId)
                        && !er.getId().equals(id) && er.getCapacity() > nextMax);
        if (otherAssignmentsExceedDesign) {
            return ApiResponse.error("Có ca thi khác đang sử dụng phòng này với sức chứa lớn hơn " + nextMax +
                    ". Không thể giảm sức chứa thiết kế.");
        }

        physicalRoom.setRoomNumber(nextNumber);
        physicalRoom.setBuilding(nextBuilding);
        physicalRoom.setCapacity(nextMax);
        roomRepository.save(physicalRoom);

        existing.setExamId(nextExamId);
        existing.setRoomId(nextRoomId);
        existing.setExam(examOpt.get());
        existing.setRoom(physicalRoom);
        existing.setCapacity(nextCapacity);
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(SYSTEM_ADMIN_ID);

        ExamRoom saved = examRoomRepository.save(existing);
        return ApiResponse.success("Cập nhật thông tin phòng thi thành công", toDto(saved));
    }

    @Transactional
    public ApiResponse<Object> softDelete(UUID id) {
        Optional<ExamRoom> opt = examRoomRepository.findActiveById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy phòng thi");
        }

        long currentCount = examRegistrationRepository.countByExamRoomIdAndIsActiveTrueAndDeletedAtIsNull(id);
        if (currentCount > 0) {
            return ApiResponse.error("Không thể xóa phòng thi này vì đang có " + currentCount + " thí sinh được phân bổ!");
        }

        ExamRoom existing = opt.get();
        existing.setIsActive(false);
        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(SYSTEM_ADMIN_ID);
        examRoomRepository.save(existing);
        return ApiResponse.successMessage("Xóa mềm phòng thi thành công");
    }

    private ExamRoomResponseDto toDto(ExamRoom er) {
        ExamRoomResponseDto dto = new ExamRoomResponseDto();
        dto.setId(er.getId());
        dto.setExamId(er.getExamId());
        if (er.getExam() != null) {
            if (er.getExam().getCourse() != null) {
                dto.setCourseCode(er.getExam().getCourse().getCourseCode());
                dto.setCourseName(er.getExam().getCourse().getCourseName());
            }
            if (er.getExam().getExamDate() != null) {
                dto.setExamDate(er.getExam().getExamDate().toString());
            }
            if (er.getExam().getStartTime() != null) {
                dto.setStartTime(er.getExam().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            if (er.getExam().getEndTime() != null) {
                dto.setEndTime(er.getExam().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }

        dto.setRoomId(er.getRoomId());
        if (er.getRoom() != null) {
            dto.setRoomNumber(er.getRoom().getRoomNumber());
            dto.setBuilding(er.getRoom().getBuilding());
            dto.setRoomMaxCapacity(er.getRoom().getCapacity());
        }

        dto.setCapacity(er.getCapacity());
        dto.setCurrentRegistrations(examRegistrationRepository.countByExamRoomIdAndIsActiveTrueAndDeletedAtIsNull(er.getId()));
        return dto;
    }
}
