package com.example.quanlydaotao.n7.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n7.dto.request.ScheduleRequest;
import com.example.quanlydaotao.n7.dto.response.ScheduleResponse;
import com.example.quanlydaotao.n7.dto.response.CourseSectionResponse;
import com.example.quanlydaotao.n7.dto.response.ShiftOption;
import com.example.quanlydaotao.n7.entity.Course;
import com.example.quanlydaotao.n7.entity.CourseSection;
import com.example.quanlydaotao.n7.entity.Room;
import com.example.quanlydaotao.n7.entity.Schedule;
import com.example.quanlydaotao.n7.repository.RoomRepository;
import com.example.quanlydaotao.n7.repository.ScheduleRepository;

@Service
@Transactional(readOnly = true)
public class ScheduleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleService.class);
    private static final String SCHEDULE_LOCK_RESOURCE = "it24m_schedule_conflict";

    private static final List<ShiftOption> SHIFTS = List.of(
            new ShiftOption(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Ca 1", 1, 5),
            new ShiftOption(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Ca 2", 6, 10),
            new ShiftOption(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Ca 3", 11, 15));

    private final ScheduleRepository scheduleRepository;
    private final RoomRepository roomRepository;
    private final CourseSectionService courseSectionService;
    private final JdbcTemplate jdbcTemplate;

    public ScheduleService(
            ScheduleRepository scheduleRepository,
            RoomRepository roomRepository,
            CourseSectionService courseSectionService,
            JdbcTemplate jdbcTemplate) {
        this.scheduleRepository = scheduleRepository;
        this.roomRepository = roomRepository;
        this.courseSectionService = courseSectionService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Page<ScheduleResponse> list(
            String keyword,
            UUID roomId,
            UUID courseSectionId,
            UUID semesterId,
            Integer dayOfWeek,
            LocalDate fromDate,
            LocalDate toDate,
            Integer year,
            Boolean active,
            Pageable pageable) {
        validateDay(dayOfWeek);
        if (year != null && (year < 1900 || year > 2100)) {
            throw badRequest("Năm lọc phải nằm trong khoảng 1900 đến 2100");
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw badRequest("Ngày bắt đầu lọc phải trước hoặc bằng ngày kết thúc");
        }
        LocalDateTime start = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime end = toDate == null ? null : toDate.atTime(23, 59, 59, 999_999_900);
        LocalDateTime yearStart = year == null ? null : LocalDate.of(year, Month.JANUARY, 1).atStartOfDay();
        LocalDateTime yearEnd = year == null ? null
                : LocalDate.of(year, Month.DECEMBER, 31).atTime(23, 59, 59, 999_999_900);
        return scheduleRepository.search(
                        blankToNull(keyword), roomId, courseSectionId, semesterId,
                        dayOfWeek, start, end, yearStart, yearEnd, active, pageable)
                .map(this::toResponse);
    }

    public List<ShiftOption> shifts() {
        return SHIFTS;
    }

    public String shiftName(UUID shiftId) {
        return SHIFTS.stream()
                .filter(shift -> shift.id().equals(shiftId))
                .map(ShiftOption::name)
                .findFirst()
                .orElse("");
    }

    public ScheduleResponse get(UUID id) {
        return toResponse(find(id));
    }

    public List<ScheduleResponse> byCourseSection(UUID courseSectionId) {
        courseSectionService.find(courseSectionId);
        return scheduleRepository
                .findByCourseSection_IdAndDeletedAtIsNullOrderByDayOfWeekAscStartPeriodAsc(courseSectionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ScheduleResponse> weekly(LocalDate date) {
        LocalDate monday = (date == null ? LocalDate.now() : date)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStart = monday.atStartOfDay();
        LocalDateTime weekEnd = monday.plusDays(6).atTime(23, 59, 59, 999_999_900);
        return scheduleRepository.search(
                        null,
                        null,
                        null,
                        null,
                        null,
                        weekStart,
                        weekEnd,
                        null,
                        null,
                        true,
                        Pageable.unpaged())
                .stream()
                .filter(schedule -> schedule.getDayOfWeek() != null
                        && schedule.getStartDate() != null
                        && schedule.getEndDate() != null
                        && hasWeekdayOccurrence(
                                later(weekStart, schedule.getStartDate()),
                                earlier(weekEnd, schedule.getEndDate()),
                                schedule.getDayOfWeek()))
                .map(this::toResponse)
                .toList();
    }

    public List<ScheduleResponse> weeklyForStudent(UUID studentId, LocalDate date) {
        return forStudentWeek(resolveStudentId(studentId), date);
    }

    public List<ScheduleResponse> weeklyForStudentCode(String studentCode, LocalDate date) {
        return forStudentWeek(resolveStudentCode(studentCode), date);
    }

    public List<ScheduleResponse> weeklyForLecturer(UUID lecturerId, LocalDate date) {
        return forWeek(scheduleRepository.findForLecturer(resolveLecturerId(lecturerId)), date);
    }

    public List<ScheduleResponse> weeklyForLecturerCode(String lecturerCode, LocalDate date) {
        return forWeek(scheduleRepository.findForLecturer(resolveLecturerCode(lecturerCode)), date);
    }

    public List<CourseSectionResponse> courseSectionsForStudentCode(String studentCode) {
        return courseSectionService.byStudent(resolveStudentCode(studentCode), null);
    }

    public List<CourseSectionResponse> courseSectionsForLecturerCode(String lecturerCode) {
        resolveLecturerCode(lecturerCode);
        return courseSectionService.list(null, true);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ScheduleResponse createForPerson(String audience, String code, ScheduleRequest request) {
        if ("student".equals(audience)) {
            UUID studentId = resolveStudentCode(code);
            requireStudentSection(studentId, request == null ? null : request.courseSectionId());
            return create(request);
        }
        if ("lecturer".equals(audience)) {
            UUID employeeId = resolveLecturerEmployeeCode(code);
            return create(withEmployeeId(request, employeeId));
        }
        throw badRequest("Äá»‘i tÆ°á»£ng lá»‹ch khÃ´ng há»£p lá»‡");
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ScheduleResponse updateForPerson(String audience, String code, UUID scheduleId, ScheduleRequest request) {
        Schedule existing = find(scheduleId);
        if ("student".equals(audience)) {
            UUID studentId = resolveStudentCode(code);
            requireStudentSection(studentId, existing.getCourseSection().getId());
            requireStudentSection(studentId, request == null ? null : request.courseSectionId());
            return update(scheduleId, request);
        }
        if ("lecturer".equals(audience)) {
            UUID employeeId = resolveLecturerEmployeeCode(code);
            if (!employeeId.equals(existing.getEmployeeId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y lá»‹ch cá»§a giáº£ng viÃªn nÃ y");
            }
            return update(scheduleId, withEmployeeId(request, employeeId));
        }
        throw badRequest("Äá»‘i tÆ°á»£ng lá»‹ch khÃ´ng há»£p lá»‡");
    }

    @Transactional
    public void deleteForPerson(String audience, String code, UUID scheduleId) {
        Schedule existing = find(scheduleId);
        if ("student".equals(audience)) {
            requireStudentSection(resolveStudentCode(code), existing.getCourseSection().getId());
        } else if ("lecturer".equals(audience)) {
            UUID employeeId = resolveLecturerEmployeeCode(code);
            if (!employeeId.equals(existing.getEmployeeId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y lá»‹ch cá»§a giáº£ng viÃªn nÃ y");
            }
        } else {
            throw badRequest("Äá»‘i tÆ°á»£ng lá»‹ch khÃ´ng há»£p lá»‡");
        }
        delete(scheduleId);
    }

    private UUID resolveLecturerEmployeeCode(String code) {
        String normalized = code == null ? "" : code.trim();
        List<UUID> ids = jdbcTemplate.query("""
                select top (1) e.id from employees e
                left join lecturers l on e.user_id is not null and l.user_id = e.user_id and l.deleted_at is null
                where e.code = ? or l.lecturer_code = ?
                order by case when e.code = ? then 0 else 1 end, e.id
                """, (rs, row) -> rs.getObject("id", UUID.class), normalized, normalized, normalized);
        if (!ids.isEmpty()) return ids.get(0);
        return resolveLecturerCode(normalized);
    }

    private void requireStudentSection(UUID studentId, UUID sectionId) {
        if (sectionId == null) throw badRequest("Lá»›p há»c pháº§n lÃ  báº¯t buá»™c");
        Integer enrolled = jdbcTemplate.queryForObject("""
                select case when exists (
                    select 1 from student_course_sections
                    where student_id = ? and course_section_id = ? and deleted_at is null
                      and coalesce(is_active, 1) = 1
                ) then 1 else 0 end
                """, Integer.class, studentId, sectionId);
        if (!Integer.valueOf(1).equals(enrolled)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chá»‰ cÃ³ thá»ƒ quáº£n lÃ½ lá»‹ch cá»§a lá»›p Ä‘Ã£ Ä‘Äƒng kÃ½");
        }
    }

    private ScheduleRequest withEmployeeId(ScheduleRequest request, UUID employeeId) {
        if (request == null) throw badRequest("ThÃ´ng tin lá»‹ch lÃ  báº¯t buá»™c");
        return new ScheduleRequest(request.courseSectionId(), request.roomId(), employeeId,
                request.dayOfWeek(), request.daysOfWeek(), request.dateId(), request.shiftId(), request.startPeriod(), request.endPeriod(),
                request.numberOfPeriods(), request.startDate(), request.endDate(), request.mode(), request.status(),
                request.description(), request.scheduleStatus(), request.note(), request.active());
    }

    private UUID resolveStudentId(UUID id) {
        List<UUID> academicIds = jdbcTemplate.query(
                "select id from students_ where id = ? and deleted_at is null",
                (rs, row) -> rs.getObject("id", UUID.class), id);
        if (!academicIds.isEmpty()) return academicIds.get(0);
        List<String> legacyCodes = jdbcTemplate.query(
                "select student_code from students where id = ?", (rs, row) -> rs.getString("student_code"), id);
        if (!legacyCodes.isEmpty()) return findAcademicStudentByCode(legacyCodes.get(0));
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sinh viên theo UUID");
    }

    private UUID resolveStudentCode(String code) {
        String normalized = code == null ? "" : code.trim();
        if (normalized.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã sinh viên là bắt buộc");
        return findAcademicStudentByCode(normalized);
    }

    private UUID findAcademicStudentByCode(String code) {
        List<UUID> ids = jdbcTemplate.query(
                "select top (1) id from students_ where code = ? and deleted_at is null order by id",
                (rs, row) -> rs.getObject("id", UUID.class), code);
        if (!ids.isEmpty()) return ids.get(0);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hồ sơ học vụ của sinh viên");
    }

    private List<ScheduleResponse> forStudentWeek(UUID studentId, LocalDate date) {
        List<Schedule> schedules = scheduleRepository.findForStudent(studentId);
        if (schedules.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sinh viên chưa có lịch học được đăng ký");
        }
        return forWeek(schedules, date);
    }

    private UUID resolveLecturerId(UUID id) {
        List<UUID> ids = jdbcTemplate.query(
                "select id from lecturers where id = ? and deleted_at is null",
                (rs, row) -> rs.getObject("id", UUID.class), id);
        if (!ids.isEmpty()) return ids.get(0);
        List<UUID> employeeIds = jdbcTemplate.query(
                "select id from employees where id = ? and deleted_at is null",
                (rs, row) -> rs.getObject("id", UUID.class), id);
        if (!employeeIds.isEmpty()) return employeeIds.get(0);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên theo UUID");
    }

    private UUID resolveLecturerCode(String code) {
        String normalized = code == null ? "" : code.trim();
        List<UUID> ids = jdbcTemplate.query(
                "select top (1) id from lecturers where lecturer_code = ? and deleted_at is null order by id",
                (rs, row) -> rs.getObject("id", UUID.class), normalized);
        if (!ids.isEmpty()) return ids.get(0);
        List<UUID> employeeIds = jdbcTemplate.query(
                "select top (1) id from employees where code = ? and deleted_at is null order by id",
                (rs, row) -> rs.getObject("id", UUID.class), normalized);
        if (!employeeIds.isEmpty()) return employeeIds.get(0);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên theo mã");
    }

    private List<ScheduleResponse> forWeek(List<Schedule> candidates, LocalDate date) {
        LocalDate monday = (date == null ? LocalDate.now() : date)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStart = monday.atStartOfDay();
        LocalDateTime weekEnd = monday.plusDays(6).atTime(23, 59, 59, 999_999_900);
        return candidates.stream()
                .filter(schedule -> schedule.getDayOfWeek() != null && schedule.getStartDate() != null
                        && schedule.getEndDate() != null
                        && hasWeekdayOccurrence(later(weekStart, schedule.getStartDate()),
                                earlier(weekEnd, schedule.getEndDate()), schedule.getDayOfWeek()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ScheduleResponse create(ScheduleRequest request) {
        List<Integer> days = effectiveDays(request);
        if (days.size() == 1) {
            ScheduleRequest singleDay = withSingleDay(request, days.get(0));
            validate(singleDay);
            lockScheduleChanges();
            CourseSection courseSection = courseSectionService.find(singleDay.courseSectionId());
            Room room = findRoom(singleDay.roomId());
            validateRoom(room);
            rejectConflicts(null, singleDay, room);
            Schedule schedule = new Schedule();
            apply(schedule, singleDay, courseSection, room);
            return toResponse(scheduleRepository.save(schedule));
        }
        // Multiple days: create one schedule per day
        lockScheduleChanges();
        ScheduleResponse firstResult = null;
        for (Integer day : days) {
            ScheduleRequest dayRequest = withSingleDay(request, day);
            validate(dayRequest);
            CourseSection courseSection = courseSectionService.find(dayRequest.courseSectionId());
            Room room = findRoom(dayRequest.roomId());
            validateRoom(room);
            rejectConflicts(null, dayRequest, room);
            Schedule schedule = new Schedule();
            apply(schedule, dayRequest, courseSection, room);
            ScheduleResponse saved = toResponse(scheduleRepository.save(schedule));
            if (firstResult == null) firstResult = saved;
        }
        return firstResult;
    }

    private List<Integer> effectiveDays(ScheduleRequest request) {
        if (request.daysOfWeek() != null && !request.daysOfWeek().isEmpty()) {
            for (Integer day : request.daysOfWeek()) {
                validateDay(day);
            }
            return request.daysOfWeek();
        }
        if (request.dayOfWeek() != null) {
            return List.of(request.dayOfWeek());
        }
        throw badRequest("Vui lòng chọn ít nhất một thứ trong tuần");
    }

    private ScheduleRequest withSingleDay(ScheduleRequest request, Integer day) {
        return new ScheduleRequest(
                request.courseSectionId(), request.roomId(), request.employeeId(),
                day, List.of(day), request.dateId(), request.shiftId(),
                request.startPeriod(), request.endPeriod(), request.numberOfPeriods(),
                request.startDate(), request.endDate(), request.mode(), request.status(),
                request.description(), request.scheduleStatus(), request.note(), request.active());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ScheduleResponse update(UUID id, ScheduleRequest request) {
        validate(request);
        Schedule schedule = find(id);
        CourseSection courseSection = courseSectionService.find(request.courseSectionId());
        Room room = findRoom(request.roomId());
        if (placementChanged(schedule, request, courseSection, room)) {
            lockScheduleChanges();
            validateRoom(room);
            rejectConflicts(id, request, room);
        }

        apply(schedule, request, courseSection, room);
        return toResponse(scheduleRepository.save(schedule));
    }

    private boolean placementChanged(Schedule existing, ScheduleRequest request,
            CourseSection courseSection, Room room) {
        return !Objects.equals(existing.getCourseSection().getId(), courseSection.getId())
                || !Objects.equals(existing.getRoom().getId(), room.getId())
                || !Objects.equals(existing.getEmployeeId(), request.employeeId())
                || !Objects.equals(existing.getShiftId(), request.shiftId())
                || !Objects.equals(existing.getDayOfWeek(), request.dayOfWeek())
                || !Objects.equals(existing.getStartPeriod(), request.startPeriod())
                || !Objects.equals(existing.getEndPeriod(), request.endPeriod())
                || !sameScheduleDate(existing.getStartDate(), request.startDate())
                || !sameScheduleDate(existing.getEndDate(), request.endDate());
    }

    private boolean sameScheduleDate(LocalDateTime existing, LocalDateTime requested) {
        return existing != null && requested != null
                && existing.toLocalDate().equals(requested.toLocalDate());
    }

    @Transactional
    public void delete(UUID id) {
        Schedule schedule = find(id);
        schedule.setDeletedAt(LocalDateTime.now());
        schedule.setActive(false);
        scheduleRepository.save(schedule);
    }

    ScheduleResponse toResponse(Schedule schedule) {
        CourseSection section = schedule.getCourseSection();
        Course course = section.getCourse();
        Room room = schedule.getRoom();
        return new ScheduleResponse(
                schedule.getId(),
                section.getId(),
                section.getSemesterId(),
                section.getCode(),
                section.getName(),
                course == null ? null : course.getCode(),
                course == null ? null : course.getName(),
                room.getId(),
                room.getRoomCode(),
                room.getRoomName(),
                room.getBuilding().getId(),
                room.getBuilding().getCode(),
                room.getBuilding().getName(),
                schedule.getEmployeeId(),
                lecturerName(schedule.getEmployeeId()),
                schedule.getDayOfWeek(),
                schedule.getDateId(),
                schedule.getShiftId(),
                schedule.getStartPeriod(),
                schedule.getEndPeriod(),
                schedule.getNumberOfPeriods(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getMode(),
                schedule.getStatus(),
                schedule.getDescription(),
                schedule.getScheduleStatus(),
                schedule.getNote(),
                schedule.getActive(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt());
    }

    private String lecturerName(UUID employeeId) {
        if (employeeId == null) {
            return null;
        }
        return jdbcTemplate.queryForList(
                        "select full_name from employees where id = ?",
                        String.class,
                        employeeId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private Room findRoom(UUID id) {
        return roomRepository.findById(id)
                .filter(room -> room.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng học"));
    }

    private void apply(
            Schedule schedule,
            ScheduleRequest request,
            CourseSection courseSection,
            Room room) {
        schedule.setCourseSection(courseSection);
        schedule.setRoom(room);
        schedule.setEmployeeId(request.employeeId());
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setDateId(request.dateId());
        schedule.setShiftId(request.shiftId());
        schedule.setStartPeriod(request.startPeriod());
        schedule.setEndPeriod(request.endPeriod());
        schedule.setNumberOfPeriods(
                request.numberOfPeriods() == null
                        ? request.endPeriod() - request.startPeriod() + 1
                        : request.numberOfPeriods());
        schedule.setStartDate(request.startDate());
        schedule.setEndDate(request.endDate());
        schedule.setMode(trimToNull(request.mode()));
        schedule.setStatus(trimToNull(request.status()));
        schedule.setDescription(trimToNull(request.description()));
        schedule.setScheduleStatus(trimToNull(request.scheduleStatus()));
        schedule.setNote(trimToNull(request.note()));
        if (request.active() != null) {
            schedule.setActive(request.active());
        }
    }

    private void rejectConflicts(UUID excludeId, ScheduleRequest request, Room room) {
        List<Schedule> conflicts = scheduleRepository.findPotentialConflicts(
                excludeId,
                room.getId(),
                request.courseSectionId(),
                request.employeeId(),
                request.dayOfWeek(),
                request.startDate(),
                request.endDate(),
                request.startPeriod(),
                request.endPeriod());
        List<Schedule> studentConflicts = scheduleRepository.findStudentEnrollmentConflicts(
                excludeId,
                request.courseSectionId(),
                request.dayOfWeek(),
                request.startDate(),
                request.endDate(),
                request.startPeriod(),
                request.endPeriod());
        Schedule scheduleConflict = firstOverlappingOccurrence(conflicts, request);
        if (scheduleConflict != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    describeScheduleConflict(scheduleConflict, request, room, false));
        }
        Schedule studentConflict = firstOverlappingOccurrence(studentConflicts, request);
        if (studentConflict != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    describeScheduleConflict(studentConflict, request, room, true));
        }
    }

    private Schedule firstOverlappingOccurrence(List<Schedule> candidates, ScheduleRequest request) {
        return candidates.stream()
                .filter(existing -> overlapsOnWeekday(
                        request.startDate(),
                        request.endDate(),
                        existing.getStartDate(),
                        existing.getEndDate(),
                        request.dayOfWeek()))
                .findFirst()
                .orElse(null);
    }

    private String describeScheduleConflict(
            Schedule existing,
            ScheduleRequest request,
            Room room,
            boolean studentConflict) {
        String cause;
        if (studentConflict) {
            cause = "Sinh viên của lớp học phần này";
        } else if (existing.getRoom() != null
                && Objects.equals(existing.getRoom().getId(), room.getId())) {
            cause = "Phòng " + room.getRoomCode();
        } else if (existing.getCourseSection() != null
                && Objects.equals(existing.getCourseSection().getId(), request.courseSectionId())) {
            cause = "Lớp học phần " + existing.getCourseSection().getCode();
        } else {
            cause = "Giảng viên";
        }

        LocalDate overlapStart = later(request.startDate(), existing.getStartDate()).toLocalDate();
        LocalDate overlapEnd = earlier(request.endDate(), existing.getEndDate()).toLocalDate();
        int daysUntilOccurrence = Math.floorMod(
                request.dayOfWeek() - overlapStart.getDayOfWeek().getValue(), 7);
        LocalDate occurrenceDate = overlapStart.plusDays(daysUntilOccurrence);
        String sectionCode = existing.getCourseSection() == null
                ? "không rõ lớp"
                : existing.getCourseSection().getCode();
        return cause + " bị trùng với lịch " + sectionCode + " vào ngày " + occurrenceDate
                + ", tiết " + Math.max(request.startPeriod(), existing.getStartPeriod())
                + "-" + Math.min(request.endPeriod(), existing.getEndPeriod()) + ".";
    }

    void lockScheduleChanges() {
        DataSource dataSource = Objects.requireNonNull(jdbcTemplate.getDataSource());
        Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean lockAcquired = false;
        boolean releaseAfterTransaction = false;
        try {
            int result = acquireScheduleLock(connection);
            if (result < 0) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Không thể lấy khóa kiểm tra lịch học từ SQL Server (mã "
                                + result + "). Vui lòng thử lại.");
            }
            lockAcquired = true;
            if (!TransactionSynchronizationManager.isActualTransactionActive()
                    || !TransactionSynchronizationManager.isSynchronizationActive()) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Không có transaction để giữ khóa kiểm tra lịch học.");
            }

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public int getOrder() {
                    return Ordered.HIGHEST_PRECEDENCE;
                }

                @Override
                public void afterCompletion(int status) {
                    try {
                        releaseScheduleLock(connection);
                    } catch (SQLException exception) {
                        LOGGER.error("Failed to release SQL Server schedule lock", exception);
                    } finally {
                        DataSourceUtils.releaseConnection(connection, dataSource);
                    }
                }
            });
            releaseAfterTransaction = true;
        } catch (SQLException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Không thể khóa kiểm tra lịch học trên SQL Server.",
                    exception);
        } finally {
            if (!releaseAfterTransaction) {
                if (lockAcquired) {
                    try {
                        releaseScheduleLock(connection);
                    } catch (SQLException exception) {
                        LOGGER.error("Failed to release SQL Server schedule lock after acquisition failure", exception);
                    }
                }
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    private int acquireScheduleLock(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                DECLARE @lockResult int;
                EXEC @lockResult = sys.sp_getapplock
                    @Resource = ?,
                    @LockMode = 'Exclusive',
                    @LockOwner = 'Session',
                    @LockTimeout = ?;
                SELECT @lockResult;
                """)) {
            statement.setString(1, SCHEDULE_LOCK_RESOURCE);
            statement.setInt(2, 10000);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("SQL Server returned no application-lock result");
                }
                return result.getInt(1);
            }
        }
    }

    private void releaseScheduleLock(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                DECLARE @releaseResult int;
                EXEC @releaseResult = sys.sp_releaseapplock
                    @Resource = ?,
                    @LockOwner = 'Session';
                SELECT @releaseResult;
                """)) {
            statement.setString(1, SCHEDULE_LOCK_RESOURCE);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || result.getInt(1) < 0) {
                    throw new SQLException("SQL Server failed to release the schedule application lock");
                }
            }
        }
    }

    private void validateRoom(Room room) {
        if (!room.isActive() || room.getDeletedAt() != null
                || (room.getStatus() != null
                        && (room.getStatus().equalsIgnoreCase("MAINTENANCE")
                                || room.getStatus().equalsIgnoreCase("INACTIVE")
                                || room.getStatus().equalsIgnoreCase("UNAVAILABLE")))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phòng học hiện không thể xếp lịch");
        }
    }

    private void validate(ScheduleRequest request) {
        if (request == null || request.courseSectionId() == null || request.roomId() == null
                || request.dayOfWeek() == null || request.shiftId() == null || request.startPeriod() == null
                || request.endPeriod() == null || request.startDate() == null || request.endDate() == null) {
            throw badRequest("Học phần, phòng, thứ, ca, tiết bắt đầu/kết thúc và khoảng ngày là bắt buộc");
        }
        validateDay(request.dayOfWeek());
        if (request.startPeriod() < 1 || request.endPeriod() < request.startPeriod()) {
            throw badRequest("Khoảng tiết học không hợp lệ");
        }
        int periodCount = request.endPeriod() - request.startPeriod() + 1;
        if (request.numberOfPeriods() != null && request.numberOfPeriods() != periodCount) {
            throw badRequest("Số tiết phải khớp với tiết bắt đầu và tiết kết thúc");
        }
        if (request.shiftId() != null) {
            ShiftOption shift = SHIFTS.stream()
                    .filter(option -> option.id().equals(request.shiftId()))
                    .findFirst()
                    .orElseThrow(() -> badRequest("Ca học không hợp lệ"));
            if (!shiftCovers(shift, request.startPeriod(), request.endPeriod())) {
                throw badRequest("Tiết học phải nằm trong khoảng của " + shift.name());
            }
        }
        if (request.startDate().isAfter(request.endDate())) {
            throw badRequest("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
        }
        if (!hasWeekdayOccurrence(request.startDate(), request.endDate(), request.dayOfWeek())) {
            throw badRequest("Khoảng ngày không chứa thứ đã chọn");
        }
        checkLength(request.mode(), 100, "Hình thức");
        checkLength(request.status(), 255, "Trạng thái");
        checkLength(request.description(), 255, "Mô tả");
        checkLength(request.scheduleStatus(), 50, "Trạng thái lịch");
        checkLength(request.note(), 255, "Ghi chú");
    }

    static boolean hasWeekdayOccurrence(LocalDateTime start, LocalDateTime end, int dayOfWeek) {
        int daysUntilDay = Math.floorMod(dayOfWeek - start.getDayOfWeek().getValue(), 7);
        return !start.toLocalDate().plusDays(daysUntilDay).isAfter(end.toLocalDate());
    }

    static boolean overlapsOnWeekday(
            LocalDateTime firstStart,
            LocalDateTime firstEnd,
            LocalDateTime secondStart,
            LocalDateTime secondEnd,
            int dayOfWeek) {
        if (firstStart == null || firstEnd == null || secondStart == null || secondEnd == null) {
            return false;
        }
        LocalDateTime overlapStart = firstStart.isAfter(secondStart) ? firstStart : secondStart;
        LocalDateTime overlapEnd = firstEnd.isBefore(secondEnd) ? firstEnd : secondEnd;
        return !overlapStart.isAfter(overlapEnd)
                && hasWeekdayOccurrence(overlapStart, overlapEnd, dayOfWeek);
    }

    static boolean shiftCovers(ShiftOption shift, Integer startPeriod, Integer endPeriod) {
        return shift != null && startPeriod != null && endPeriod != null
                && startPeriod >= shift.startPeriod() && endPeriod <= shift.endPeriod()
                && endPeriod >= startPeriod;
    }

    private void validateDay(Integer dayOfWeek) {
        if (dayOfWeek != null && (dayOfWeek < 1 || dayOfWeek > 7)) {
            throw badRequest("Thứ trong tuần phải nằm trong khoảng 1 đến 7 (thứ Hai đến Chủ nhật)");
        }
    }

    private LocalDateTime later(LocalDateTime first, LocalDateTime second) {
        return first.isAfter(second) ? first : second;
    }

    private LocalDateTime earlier(LocalDateTime first, LocalDateTime second) {
        return first.isBefore(second) ? first : second;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String trimToNull(String value) {
        return value == null ? null : value.trim();
    }

    private void checkLength(String value, int max, String field) {
        if (value != null && value.length() > max) {
            throw badRequest(field + " không được dài quá " + max + " ký tự");
        }
    }

    private Schedule find(UUID id) {
        return scheduleRepository.findById(id)
                .filter(schedule -> schedule.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch học"));
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
