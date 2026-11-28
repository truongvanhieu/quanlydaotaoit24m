package com.exam.service;

import com.exam.dto.ApiResponse;
import com.exam.dto.ExamRegistrationRequestDto;
import com.exam.dto.ExamRegistrationResponseDto;
import com.exam.dto.PaginationDto;
import com.exam.entity.Exam;
import com.exam.entity.ExamRegistration;
import com.exam.entity.ExamResult;
import com.exam.entity.ExamRoom;
import com.exam.repository.ExamRegistrationRepository;
import com.exam.repository.ExamRepository;
import com.exam.repository.ExamResultRepository;
import com.exam.repository.ExamRoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDate;
import com.exam.entity.Room;
import com.exam.entity.Student;
import com.exam.repository.RoomRepository;
import com.exam.repository.StudentRepository;
import java.util.stream.Collectors;

@Service
public class ExamRegistrationService {

    private static final UUID SYSTEM_ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final ExamRegistrationRepository examRegistrationRepository;
    private final ExamRoomRepository examRoomRepository;
    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final RoomRepository roomRepository;
    private final StudentRepository studentRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    public ExamRegistrationService(
            ExamRegistrationRepository examRegistrationRepository,
            ExamRoomRepository examRoomRepository,
            ExamRepository examRepository,
            ExamResultRepository examResultRepository, RoomRepository roomRepository, StudentRepository studentRepository, org.springframework.jdbc.core.JdbcTemplate jdbc) {
        this.examRegistrationRepository = examRegistrationRepository;
        this.examRoomRepository = examRoomRepository;
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.roomRepository = roomRepository;
        this.studentRepository = studentRepository;
        this.jdbc = jdbc;
    }

    @jakarta.annotation.PostConstruct
    public void ensureBirthDateColumn() {
        jdbc.execute("IF COL_LENGTH('dbo.students', 'birth_date') IS NULL ALTER TABLE dbo.students ADD birth_date DATE NULL");
        jdbc.execute("IF COL_LENGTH('dbo.exam_registrations', 'merged_group_id') IS NULL ALTER TABLE dbo.exam_registrations ADD merged_group_id UNIQUEIDENTIFIER NULL");
    }

    public ApiResponse<Object> getAll(int page, int limit, String search, UUID examId, UUID examRoomId, UUID studentId, String groupType, UUID groupId, String studentClass) {
        String querySearch = (search != null && !search.trim().isEmpty()) ? "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%" : null;

        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id")));
        Page<ExamRegistration> resultPage = examRegistrationRepository.searchActive(querySearch, examId, examRoomId, studentId, groupType, groupId, studentClass, pageRequest);

        List<ExamRegistrationResponseDto> dtoList = resultPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        PaginationDto pagination = new PaginationDto(
                page, limit, resultPage.getTotalElements(), resultPage.getTotalPages() == 0 ? 1 : resultPage.getTotalPages()
        );
        return ApiResponse.successPage(dtoList, pagination);
    }

    public ApiResponse<Object> getById(UUID id) {
        Optional<ExamRegistration> opt = examRegistrationRepository.findActiveById(id);
        if (opt.isPresent()) {
            return ApiResponse.successData(toDto(opt.get()));
        }
        return ApiResponse.error("Không tìm thấy thông tin đăng ký thí sinh");
    }

    @Transactional
    public ApiResponse<Object> create(ExamRegistrationRequestDto req) {
        if (req.getExamId() == null || req.getExamRoomId() == null || req.getStudentId() == null) {
            return ApiResponse.error("Vui lòng chọn kỳ thi, phòng thi và sinh viên");
        }

        if (examRepository.lockActiveById(req.getExamId()).isEmpty()) return ApiResponse.error("Ca thi không tồn tại");
        Optional<ExamRoom> examRoomOpt = examRoomRepository.findActiveById(req.getExamRoomId());
        if (examRoomOpt.isEmpty() || !examRoomOpt.get().getExamId().equals(req.getExamId())) {
            return ApiResponse.error("Phòng thi đã chọn không thuộc ca thi này hoặc không tồn tại");
        }

        ExamRoom examRoom = examRoomOpt.get();
        String roomNumber = examRoom.getRoom() != null ? examRoom.getRoom().getRoomNumber() : "Phòng";

        long currentCount = examRegistrationRepository.countByExamRoomIdAndIsActiveTrueAndDeletedAtIsNull(req.getExamRoomId());
        if (currentCount >= examRoom.getCapacity()) {
            return ApiResponse.error("Phòng " + roomNumber + " đã đầy sức chứa (" + currentCount + "/" + examRoom.getCapacity() + " thí sinh)! Không thể đăng ký thêm.");
        }

        List<ExamRegistration> existingList = examRegistrationRepository.findByExamIdAndStudentIdActive(req.getExamId(), req.getStudentId());
        if (!existingList.isEmpty()) {
            return ApiResponse.error("Sinh viên này đã được đăng ký trong ca thi này với SBD: " + existingList.get(0).getRollNumber() + "!");
        }

        String rollNumber = (req.getRollNumber() != null && !req.getRollNumber().trim().isEmpty())
                ? req.getRollNumber().trim()
                : generateRollNumber(req.getExamId());

        if (rollNumber.length() > 20 || examRegistrationRepository.findByExamIdActive(req.getExamId()).stream().anyMatch(r -> r.getRollNumber().equalsIgnoreCase(rollNumber))) {
            return ApiResponse.error("Số báo danh bị trùng trong ca thi hoặc dài quá 20 ký tự");
        }
        ExamRegistration entity = new ExamRegistration();
        entity.setExamId(req.getExamId());
        entity.setExamRoomId(req.getExamRoomId());
        entity.setStudentId(req.getStudentId());
        entity.setRollNumber(rollNumber);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(SYSTEM_ADMIN_ID);
        entity.setIsActive(true);

        ExamRegistration saved = examRegistrationRepository.save(entity);
        return ApiResponse.success("Đăng ký thí sinh thành công", toDto(saved));
    }

    @Transactional
    public ApiResponse<Object> update(UUID id, ExamRegistrationRequestDto req) {
        Optional<ExamRegistration> opt = examRegistrationRepository.findActiveById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đăng ký thí sinh");
        }

        ExamRegistration existing = opt.get();
        Exam exam = examRepository.lockActiveById(existing.getExamId()).orElse(null);
        if (exam == null) return ApiResponse.error("Ca thi không tồn tại");
        LocalDate birthDate = null;
        if (req.getBirthDate() != null && !req.getBirthDate().isBlank()) {
            try { birthDate = LocalDate.parse(req.getBirthDate()); }
            catch (java.time.DateTimeException ex) { return ApiResponse.error("Ngày sinh không hợp lệ"); }
        }
        String roll = req.getRollNumber() == null ? existing.getRollNumber() : req.getRollNumber().trim();
        if (roll.isEmpty()) roll = generateRollNumber(existing.getExamId());
        final String checkedRoll = roll;
        if (!roll.equals(existing.getRollNumber()) && (roll.length() > 20 || examRegistrationRepository.findByExamIdActive(existing.getExamId()).stream()
                .anyMatch(r -> !r.getId().equals(id) && r.getRollNumber().equalsIgnoreCase(checkedRoll)))) {
            return ApiResponse.error("Số báo danh bị trùng trong ca thi hoặc dài quá 20 ký tự");
        }
        ExamRoom target = existing.getExamRoom();
        if (req.getPhysicalRoomId() != null) {
            Room physical = roomRepository.lockActiveById(req.getPhysicalRoomId()).orElse(null);
            if (physical == null || physical.getCapacity() <= 0) return ApiResponse.error("Phòng vật lý không hợp lệ");
            if (target == null || !physical.getId().equals(target.getRoomId())) {
                if (roomBusy(exam, physical.getId())) return ApiResponse.error("Phòng đã được sử dụng bởi ca thi khác trùng giờ");
                target = examRoomRepository.findByExamIdAndRoomIdActive(exam.getId(), physical.getId()).stream().findFirst().orElse(null);
                if (target == null) {
                    target = new ExamRoom();
                    target.setExamId(exam.getId()); target.setExam(exam);
                    target.setRoomId(physical.getId()); target.setRoom(physical);
                    target.setCapacity(physical.getCapacity()); target.setCreatedBy(SYSTEM_ADMIN_ID);
                    target = examRoomRepository.saveAndFlush(target);
                }
            }
        } else if (req.getExamRoomId() != null && !req.getExamRoomId().equals(existing.getExamRoomId())) {
            target = examRoomRepository.findActiveById(req.getExamRoomId()).orElse(null);
            if (target == null || !exam.getId().equals(target.getExamId())) return ApiResponse.error("Phòng mới phải thuộc đúng ca thi");
            if (roomBusy(exam, target.getRoomId())) return ApiResponse.error("Phòng đã được sử dụng bởi ca thi khác trùng giờ");
        }
        if (target == null) return ApiResponse.error("Phòng thi không hợp lệ");
        if (!target.getId().equals(existing.getExamRoomId())) {
            long count = examRegistrationRepository.countByExamRoomIdAndIsActiveTrueAndDeletedAtIsNull(target.getId());
            if (count >= target.getCapacity()) return ApiResponse.error("Phòng mới đã đầy sức chứa");
            existing.setExamRoomId(target.getId()); existing.setExamRoom(target);
        }
        existing.setRollNumber(roll);
        if (req.getBirthDate() != null && existing.getStudent() != null) {
            existing.getStudent().setBirthDate(birthDate);
            studentRepository.save(existing.getStudent());
        }

        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(SYSTEM_ADMIN_ID);

        ExamRegistration saved = examRegistrationRepository.save(existing);
        return ApiResponse.success("Cập nhật đăng ký thí sinh thành công", toDto(saved));
    }

    private boolean roomBusy(Exam exam, UUID physicalId) {
        return examRoomRepository.findAllActive().stream().anyMatch(r -> {
            Exam other = r.getExam();
            return physicalId.equals(r.getRoomId()) && other != null && !exam.getId().equals(other.getId())
                && Boolean.TRUE.equals(other.getIsActive()) && other.getDeletedAt() == null
                && !"Đã hủy".equals(other.getExamStatus()) && !"Hoãn thi".equals(other.getExamStatus())
                && exam.getExamDate().equals(other.getExamDate())
                && exam.getStartTime().isBefore(other.getEndTime()) && other.getStartTime().isBefore(exam.getEndTime());
        });
    }

    public ApiResponse<Object> transferRooms(UUID registrationId) {
        ExamRegistration reg = examRegistrationRepository.findActiveById(registrationId).orElse(null);
        if (reg == null || reg.getExam() == null) return ApiResponse.error("Không tìm thấy đăng ký thí sinh");
        List<Map<String, Object>> result = new ArrayList<>();
        for (Room room : roomRepository.findByIsActiveTrueOrderByRoomNumberAsc()) {
            ExamRoom assigned = examRoomRepository.findByExamIdAndRoomIdActive(reg.getExamId(), room.getId()).stream().findFirst().orElse(null);
            boolean current = assigned != null && assigned.getId().equals(reg.getExamRoomId());
            int capacity = assigned == null ? room.getCapacity() : assigned.getCapacity();
            long count = assigned == null ? 0 : examRegistrationRepository.countByExamRoomIdAndIsActiveTrueAndDeletedAtIsNull(assigned.getId());
            boolean busy = !current && roomBusy(reg.getExam(), room.getId());
            Map<String, Object> row = new HashMap<>();
            row.put("id", room.getId()); row.put("room_number", room.getRoomNumber()); row.put("building", room.getBuilding());
            row.put("capacity", capacity); row.put("current_registrations", count); row.put("assigned", assigned != null);
            row.put("current", current); row.put("available", current || (!busy && count < capacity));
            row.put("reason", busy ? "Trùng giờ ca thi khác" : count >= capacity && !current ? "Đã đầy" : "");
            result.add(row);
        }
        return ApiResponse.successData(result);
    }

    @Transactional
    public ApiResponse<Object> bulkCreate(ExamRegistrationRequestDto req) {
        if (req.getExamId() == null || req.getExamRoomId() == null || req.getStudentIds() == null || req.getStudentIds().isEmpty())
            return ApiResponse.error("Vui lòng chọn ca thi, phòng và sinh viên");
        Exam exam = examRepository.lockActiveById(req.getExamId()).orElse(null);
        ExamRoom room = examRoomRepository.findActiveById(req.getExamRoomId()).orElse(null);
        if (exam == null || room == null || !exam.getId().equals(room.getExamId())) return ApiResponse.error("Phòng không thuộc ca thi");
        if (roomBusy(exam, room.getRoomId())) return ApiResponse.error("Phòng trùng giờ với ca thi khác");
        Set<UUID> ids = new LinkedHashSet<>(req.getStudentIds());
        if (ids.contains(null)) return ApiResponse.error("Sinh viên không hợp lệ");
        List<Student> students = studentRepository.findAllById(ids);
        if (students.size() != ids.size() || students.stream().anyMatch(st -> !Boolean.TRUE.equals(st.getIsActive())))
            return ApiResponse.error("Danh sách có sinh viên không hợp lệ");
        List<ExamRegistration> old = examRegistrationRepository.findByExamIdActive(exam.getId());
        if (old.stream().anyMatch(r -> ids.contains(r.getStudentId()))) return ApiResponse.error("Có sinh viên đã đăng ký ca thi này. Hãy bỏ chọn sinh viên đó");
        long count = examRegistrationRepository.countByExamRoomIdAndIsActiveTrueAndDeletedAtIsNull(room.getId());
        if (count + ids.size() > room.getCapacity()) return ApiResponse.error("Phòng chỉ còn " + (room.getCapacity() - count) + " chỗ trống");
        Set<String> used = old.stream().map(r -> r.getRollNumber().toUpperCase(Locale.ROOT)).collect(Collectors.toSet());
        // Xen kẽ sinh viên theo lớp, giữ nguyên lớp sinh hoạt gốc.
        Map<String, Queue<Student>> classes = new TreeMap<>();
        students.sort(Comparator.comparing(Student::getStudentCode));
        for (Student st : students) classes.computeIfAbsent(Objects.toString(st.getClassName(), ""), key -> new ArrayDeque<>()).add(st);
        UUID groupId = classes.keySet().stream().map(c -> c.trim().toUpperCase(Locale.ROOT)).filter(c -> !c.isBlank()).distinct().count() >= 2 ? UUID.randomUUID() : null;
        List<ExamRegistration> rows = new ArrayList<>();
        int serial = 1;
        while (rows.size() < students.size()) {
            for (Queue<Student> queue : classes.values()) {
                if (queue.isEmpty()) continue;
                Student st = queue.remove();
                String roll;
                do { roll = formatRoll(exam, serial++); } while (!used.add(roll.toUpperCase(Locale.ROOT)));
                ExamRegistration reg = new ExamRegistration();
                reg.setExamId(exam.getId()); reg.setExamRoomId(room.getId()); reg.setStudentId(st.getId());
                reg.setRollNumber(roll); reg.setCreatedBy(SYSTEM_ADMIN_ID); reg.setMergedGroupId(groupId); rows.add(reg);
            }
        }
        examRegistrationRepository.saveAllAndFlush(rows);
        Map<String, Object> result = new HashMap<>();
        result.put("count", rows.size()); result.put("merged_group_id", groupId);
        return ApiResponse.successData(result);
    }

    // Group membership is explicit; sharing a room is not proof of a merged class.
    public ApiResponse<Object> groups(UUID examId) {
        List<ExamRegistration> active = examId == null ? examRegistrationRepository.findAllActive() : examRegistrationRepository.findByExamIdActive(examId);
        Map<UUID, List<ExamRegistration>> grouped = active.stream().filter(r -> r.getMergedGroupId() != null)
                .collect(Collectors.groupingBy(ExamRegistration::getMergedGroupId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            List<ExamRegistration> rows = entry.getValue(); ExamRegistration first = rows.get(0);
            Map<String, Object> info = new HashMap<>();
            info.put("id", entry.getKey()); info.put("exam_id", first.getExamId()); info.put("count", rows.size());
            info.put("classes", rows.stream().map(r -> r.getStudent() == null ? "" : Objects.toString(r.getStudent().getClassName(), "")).filter(c -> !c.isBlank()).distinct().sorted().toList());
            info.put("rooms", rows.stream().map(r -> r.getExamRoom() == null || r.getExamRoom().getRoom() == null ? "" : Objects.toString(r.getExamRoom().getRoom().getRoomNumber(), "")).filter(c -> !c.isBlank()).distinct().sorted().toList());
            info.put("created_at", rows.stream().map(ExamRegistration::getCreatedAt).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null));
            result.add(info);
        }
        result.sort(Comparator.comparing(m -> m.get("id").toString()));
        return ApiResponse.successData(result);
    }

    @Transactional
    public ApiResponse<Object> classifyGroup(ExamRegistrationRequestDto req) {
        if (req.getExamId() == null || req.getExamRoomId() == null || req.getRegistrationIds() == null)
            return ApiResponse.error("Vui lòng chọn ca thi, phòng và các đăng ký cần gộp");
        Set<UUID> ids = new LinkedHashSet<>(req.getRegistrationIds());
        if (ids.size() < 2 || ids.contains(null)) return ApiResponse.error("Cần ít nhất hai sinh viên từ hai lớp khác nhau");
        if (examRepository.lockActiveById(req.getExamId()).isEmpty()) return ApiResponse.error("Ca thi không tồn tại");
        List<ExamRegistration> rows = examRegistrationRepository.findByExamIdActive(req.getExamId()).stream().filter(row -> ids.contains(row.getId())).toList();
        if (rows.size() != ids.size() || rows.stream().anyMatch(row -> !req.getExamRoomId().equals(row.getExamRoomId()) || row.getMergedGroupId() != null))
            return ApiResponse.error("Chỉ chọn đăng ký chưa gộp trong cùng ca và phòng thi");
        if (rows.stream().map(row -> row.getStudent() == null ? "" : Objects.toString(row.getStudent().getClassName(), "").trim().toUpperCase(Locale.ROOT)).filter(c -> !c.isBlank()).distinct().count() < 2)
            return ApiResponse.error("Cần chọn sinh viên từ ít nhất hai lớp khác nhau");
        UUID groupId = UUID.randomUUID();
        rows.forEach(row -> row.setMergedGroupId(groupId));
        examRegistrationRepository.saveAllAndFlush(rows);
        return ApiResponse.successData(Map.of("count", rows.size(), "merged_group_id", groupId));
    }

    @Transactional
    public ApiResponse<Object> softDelete(UUID id) {
        Optional<ExamRegistration> opt = examRegistrationRepository.findActiveById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy thí sinh đăng ký");
        }

        Optional<ExamResult> resultOpt = examResultRepository.findByRegistrationIdActive(id);
        if (resultOpt.isPresent() && Boolean.TRUE.equals(resultOpt.get().getIsLocked())) {
            return ApiResponse.error("Không thể xóa thí sinh này vì kết quả thi đã được chốt!");
        }

        ExamRegistration existing = opt.get();
        existing.setIsActive(false);
        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(SYSTEM_ADMIN_ID);
        examRegistrationRepository.save(existing);
        return ApiResponse.successMessage("Xóa mềm thí sinh thành công");
    }

    private String formatRoll(Exam exam, int number) {
        String code = exam != null && exam.getCourse() != null ? exam.getCourse().getCourseCode().replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT) : "EXAM";
        String suffix = String.format("-%03d", number);
        return "SBD-" + code.substring(0, Math.min(code.length(), 20 - 4 - suffix.length())) + suffix;
    }

    private String generateRollNumber(UUID examId) {
        Exam exam = examRepository.findActiveById(examId).orElse(null);
        Set<String> used = examRegistrationRepository.findByExamIdActive(examId).stream().map(r -> r.getRollNumber().toUpperCase(Locale.ROOT)).collect(Collectors.toSet());
        int i = 1;
        String roll;
        do { roll = formatRoll(exam, i++); } while (used.contains(roll.toUpperCase(Locale.ROOT)));
        return roll;
    }

    private ExamRegistrationResponseDto toDto(ExamRegistration reg) {
        ExamRegistrationResponseDto dto = new ExamRegistrationResponseDto();
        dto.setId(reg.getId());
        dto.setMergedGroupId(reg.getMergedGroupId());
        dto.setExamId(reg.getExamId());
        if (reg.getExam() != null) {
            if (reg.getExam().getCourse() != null) {
                dto.setCourseCode(reg.getExam().getCourse().getCourseCode());
                dto.setCourseName(reg.getExam().getCourse().getCourseName());
            }
            if (reg.getExam().getExamDate() != null) {
                dto.setExamDate(reg.getExam().getExamDate().toString());
            }
            if (reg.getExam().getStartTime() != null) {
                dto.setStartTime(reg.getExam().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }

        dto.setExamRoomId(reg.getExamRoomId());
        if (reg.getExamRoom() != null) {
            dto.setRoomCapacity(reg.getExamRoom().getCapacity());
            if (reg.getExamRoom().getRoom() != null) {
                dto.setRoomNumber(reg.getExamRoom().getRoom().getRoomNumber());
                dto.setBuilding(reg.getExamRoom().getRoom().getBuilding());
            }
        }

        dto.setStudentId(reg.getStudentId());
        if (reg.getStudent() != null) {
            dto.setStudentCode(reg.getStudent().getStudentCode());
            dto.setStudentName(reg.getStudent().getFullName());
            dto.setStudentClass(reg.getStudent().getClassName());
            dto.setBirthDate(reg.getStudent().getBirthDate() == null ? null : reg.getStudent().getBirthDate().toString());
        }

        dto.setRollNumber(reg.getRollNumber());

        examResultRepository.findByRegistrationIdActive(reg.getId()).ifPresent(res -> {
            dto.setResultId(res.getId());
            dto.setScore(res.getScore());
            dto.setResultStatus(res.getStatus());
            dto.setIsLocked(res.getIsLocked());
        });

        return dto;
    }
}
