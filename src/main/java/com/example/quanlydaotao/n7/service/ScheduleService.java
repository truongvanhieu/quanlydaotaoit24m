package com.example.quanlydaotao.n7.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n7.dto.request.ScheduleRequest;
import com.example.quanlydaotao.n7.dto.response.ScheduleResponse;
import com.example.quanlydaotao.n7.entity.Course;
import com.example.quanlydaotao.n7.entity.CourseSection;
import com.example.quanlydaotao.n7.entity.Room;
import com.example.quanlydaotao.n7.entity.Schedule;
import com.example.quanlydaotao.n7.repository.RoomRepository;
import com.example.quanlydaotao.n7.repository.ScheduleRepository;

@Service
@Transactional(readOnly = true)
public class ScheduleService {

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
            Integer dayOfWeek,
            LocalDate fromDate,
            LocalDate toDate,
            Boolean active,
            Pageable pageable) {
        validateDay(dayOfWeek);
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw badRequest("Ngày bắt đầu lọc phải trước hoặc bằng ngày kết thúc");
        }
        LocalDateTime start = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime end = toDate == null ? null : toDate.atTime(23, 59, 59, 999_999_900);
        return scheduleRepository.search(
                        blankToNull(keyword), roomId, courseSectionId, dayOfWeek, start, end, active, pageable)
                .map(ScheduleService::toResponse);
    }

    public ScheduleResponse get(UUID id) {
        return toResponse(find(id));
    }

    public List<ScheduleResponse> byCourseSection(UUID courseSectionId) {
        courseSectionService.find(courseSectionId);
        return scheduleRepository
                .findByCourseSection_IdAndDeletedAtIsNullOrderByDayOfWeekAscStartPeriodAsc(courseSectionId)
                .stream()
                .map(ScheduleService::toResponse)
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
                        weekStart,
                        weekEnd,
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
                .map(ScheduleService::toResponse)
                .toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ScheduleResponse create(ScheduleRequest request) {
        validate(request);
        lockScheduleChanges();
        CourseSection courseSection = courseSectionService.find(request.courseSectionId());
        Room room = findRoom(request.roomId());
        validateRoom(room);
        rejectConflicts(null, request, room);

        Schedule schedule = new Schedule();
        apply(schedule, request, courseSection, room);
        return toResponse(scheduleRepository.save(schedule));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ScheduleResponse update(UUID id, ScheduleRequest request) {
        validate(request);
        lockScheduleChanges();
        Schedule schedule = find(id);
        CourseSection courseSection = courseSectionService.find(request.courseSectionId());
        Room room = findRoom(request.roomId());
        validateRoom(room);
        rejectConflicts(id, request, room);

        apply(schedule, request, courseSection, room);
        return toResponse(scheduleRepository.save(schedule));
    }

    @Transactional
    public void delete(UUID id) {
        Schedule schedule = find(id);
        schedule.setDeletedAt(LocalDateTime.now());
        schedule.setActive(false);
        scheduleRepository.save(schedule);
    }

    static ScheduleResponse toResponse(Schedule schedule) {
        CourseSection section = schedule.getCourseSection();
        Course course = section.getCourse();
        Room room = schedule.getRoom();
        return new ScheduleResponse(
                schedule.getId(),
                section.getId(),
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
                request.dayOfWeek(),
                request.startDate(),
                request.endDate(),
                request.startPeriod(),
                request.endPeriod());
        boolean conflictingOccurrence = conflicts.stream().anyMatch(existing ->
                hasWeekdayOccurrence(
                        later(request.startDate(), existing.getStartDate()),
                        earlier(request.endDate(), existing.getEndDate()),
                        request.dayOfWeek()));
        if (conflictingOccurrence) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phòng hoặc lớp học phần đã có lịch trùng thứ, ngày và tiết học");
        }
    }

    private void lockScheduleChanges() {
        Integer result = jdbcTemplate.execute((org.springframework.jdbc.core.ConnectionCallback<Integer>) connection -> {
            try (var call = connection.prepareCall("{? = call sys.sp_getapplock(?, ?, ?, ?)}")) {
                call.registerOutParameter(1, java.sql.Types.INTEGER);
                call.setString(2, "it24m_schedule_conflict");
                call.setString(3, "Exclusive");
                call.setString(4, "Transaction");
                call.setInt(5, 10000);
                call.execute();
                return call.getInt(1);
            }
        });
        if (result == null || result < 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể khóa kiểm tra lịch học; vui lòng thử lại");
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
                || request.dayOfWeek() == null || request.startPeriod() == null
                || request.endPeriod() == null || request.startDate() == null || request.endDate() == null) {
            throw badRequest("Học phần, phòng, thứ, tiết bắt đầu/kết thúc và khoảng ngày là bắt buộc");
        }
        validateDay(request.dayOfWeek());
        if (request.startPeriod() < 1 || request.endPeriod() < request.startPeriod()) {
            throw badRequest("Khoảng tiết học không hợp lệ");
        }
        int periodCount = request.endPeriod() - request.startPeriod() + 1;
        if (request.numberOfPeriods() != null && request.numberOfPeriods() != periodCount) {
            throw badRequest("Số tiết phải khớp với tiết bắt đầu và tiết kết thúc");
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
