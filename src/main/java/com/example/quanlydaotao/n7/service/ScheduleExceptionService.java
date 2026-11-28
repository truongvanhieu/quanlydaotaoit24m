package com.example.quanlydaotao.n7.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n7.dto.request.ScheduleExceptionRequest;
import com.example.quanlydaotao.n7.dto.response.ScheduleExceptionResponse;
import com.example.quanlydaotao.n7.dto.response.ScheduleResponse;

@Service
@Transactional(readOnly = true)
public class ScheduleExceptionService {

    private final JdbcTemplate jdbc;
    private final ScheduleService scheduleService;

    public ScheduleExceptionService(JdbcTemplate jdbc, ScheduleService scheduleService) {
        this.jdbc = jdbc;
        this.scheduleService = scheduleService;
    }

    public List<ScheduleExceptionResponse> list(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null || fromDate.isAfter(toDate)) {
            throw badRequest("Cần khoảng ngày hợp lệ để tải ngoại lệ lịch");
        }
        List<ExceptionRow> rows = jdbc.query("""
                select x.id, x.schedule_id, x.occurrence_date, x.exception_type, x.replacement_date,
                       x.room_id, x.employee_id, x.start_period, x.end_period, x.note
                from schedule_exceptions x
                join schedules s on s.id = x.schedule_id
                where x.deleted_at is null and s.deleted_at is null
                  and coalesce(s.is_active, 1) = 1
                  and (x.occurrence_date between ? and ? or x.replacement_date between ? and ?)
                order by x.occurrence_date
                """, (rs, row) -> new ExceptionRow(
                rs.getObject("id", UUID.class),
                rs.getObject("schedule_id", UUID.class),
                rs.getDate("occurrence_date").toLocalDate(),
                rs.getString("exception_type"),
                localDate(rs.getDate("replacement_date")),
                rs.getObject("room_id", UUID.class),
                rs.getObject("employee_id", UUID.class),
                (Integer) rs.getObject("start_period"),
                (Integer) rs.getObject("end_period"),
                rs.getString("note")), Date.valueOf(fromDate), Date.valueOf(toDate),
                Date.valueOf(fromDate), Date.valueOf(toDate));
        return rows.stream().map(this::response).toList();
    }

    @Transactional
    public ScheduleExceptionResponse save(UUID scheduleId, ScheduleExceptionRequest request) {
        if (request == null || request.occurrenceDate() == null || request.exceptionType() == null) {
            throw badRequest("Ngày gốc và loại ngoại lệ là bắt buộc");
        }
        ScheduleResponse schedule = scheduleService.get(scheduleId);
        LocalDate start = schedule.startDate().toLocalDate();
        LocalDate end = schedule.endDate().toLocalDate();
        if (request.occurrenceDate().isBefore(start) || request.occurrenceDate().isAfter(end)
                || request.occurrenceDate().getDayOfWeek().getValue() != schedule.dayOfWeek()) {
            throw badRequest("Ngày gốc không phải buổi học lặp của lịch này");
        }

        String type = request.exceptionType().trim().toUpperCase(Locale.ROOT);
        if (!List.of("CANCELLED", "MAKEUP", "SUBSTITUTE").contains(type)) {
            throw badRequest("Loại ngoại lệ không hợp lệ");
        }
        if ("MAKEUP".equals(type)
                && (request.replacementDate() == null || request.replacementDate().equals(request.occurrenceDate()))) {
            throw badRequest("Ngày học bù phải khác ngày học gốc");
        }
        if ("SUBSTITUTE".equals(type) && request.employeeId() == null) {
            throw badRequest("Cần chọn giảng viên dạy thay");
        }

        UUID roomId = "CANCELLED".equals(type) ? null
                : request.roomId() == null ? schedule.roomId() : request.roomId();
        UUID employeeId = "CANCELLED".equals(type) ? null
                : request.employeeId() == null ? schedule.employeeId() : request.employeeId();
        Integer startPeriod = "CANCELLED".equals(type) ? null
                : request.startPeriod() == null ? schedule.startPeriod() : request.startPeriod();
        Integer endPeriod = "CANCELLED".equals(type) ? null
                : request.endPeriod() == null ? schedule.endPeriod() : request.endPeriod();
        LocalDate effectiveDate = "MAKEUP".equals(type) ? request.replacementDate() : request.occurrenceDate();

        if (!"CANCELLED".equals(type)) {
            if (startPeriod == null || endPeriod == null || startPeriod < 1 || endPeriod < startPeriod) {
                throw badRequest("Khoảng tiết học không hợp lệ");
            }
            validateRoom(roomId);
            validateEmployee(employeeId);
            scheduleService.lockScheduleChanges();
            rejectOneTimeConflict(scheduleId, schedule.courseSectionId(), request.occurrenceDate(),
                    roomId, employeeId, effectiveDate, startPeriod, endPeriod);
        }

        UUID id = jdbc.query("""
                select id from schedule_exceptions
                where schedule_id = ? and occurrence_date = ?
                """, (rs, row) -> rs.getObject("id", UUID.class),
                scheduleId, Date.valueOf(request.occurrenceDate())).stream().findFirst().orElse(null);
        String note = request.note() == null || request.note().isBlank() ? null : request.note().trim();
        if (id == null) {
            id = UUID.randomUUID();
            jdbc.update("""
                    insert into schedule_exceptions
                      (id, schedule_id, occurrence_date, exception_type, replacement_date,
                       room_id, employee_id, start_period, end_period, note)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, id, scheduleId, Date.valueOf(request.occurrenceDate()), type,
                    request.replacementDate() == null ? null : Date.valueOf(request.replacementDate()),
                    roomId, employeeId, startPeriod, endPeriod, note);
        } else {
            jdbc.update("""
                    update schedule_exceptions
                    set exception_type = ?, replacement_date = ?, room_id = ?, employee_id = ?,
                        start_period = ?, end_period = ?, note = ?, deleted_at = null,
                        updated_at = sysdatetime()
                    where id = ?
                    """, type, request.replacementDate() == null ? null : Date.valueOf(request.replacementDate()),
                    roomId, employeeId, startPeriod, endPeriod, note, id);
        }
        return response(new ExceptionRow(id, scheduleId, request.occurrenceDate(), type,
                request.replacementDate(), roomId, employeeId, startPeriod, endPeriod, note));
    }

    @Transactional
    public void delete(UUID id) {
        int changed = jdbc.update("""
                update schedule_exceptions set deleted_at = sysdatetime(), updated_at = sysdatetime()
                where id = ? and deleted_at is null
                """, id);
        if (changed == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy ngoại lệ lịch");
    }

    private void rejectOneTimeConflict(
            UUID scheduleId,
            UUID courseSectionId,
            LocalDate originalDate,
            UUID roomId,
            UUID employeeId,
            LocalDate date,
            int startPeriod,
            int endPeriod) {
        String overlapQuery = """
                select count(*) from schedules s
                where s.deleted_at is null and coalesce(s.is_active, 1) = 1
                  and s.day_of_week = ?
                  and s.start_date <= ? and s.end_date >= ?
                  and s.start_period <= ? and s.end_period >= ?
                  and not exists (
                      select 1 from schedule_exceptions x
                      where x.schedule_id = s.id and x.occurrence_date = ?
                        and x.deleted_at is null and x.exception_type in ('CANCELLED', 'MAKEUP')
                  )
                """;
        boolean skipParentOccurrence = originalDate.equals(date);
        String parentCondition = skipParentOccurrence ? " and s.id <> ?" : "";
        Integer count;
        if (employeeId == null) {
            if (skipParentOccurrence) {
                count = jdbc.queryForObject(overlapQuery + parentCondition
                                + " and (s.room_id = ? or s.course_section_id = ?)",
                        Integer.class, date.getDayOfWeek().getValue(), Date.valueOf(date), Date.valueOf(date),
                        endPeriod, startPeriod, Date.valueOf(date), scheduleId, roomId, courseSectionId);
            } else {
                count = jdbc.queryForObject(overlapQuery + " and (s.room_id = ? or s.course_section_id = ?)",
                        Integer.class, date.getDayOfWeek().getValue(), Date.valueOf(date), Date.valueOf(date),
                        endPeriod, startPeriod, Date.valueOf(date), roomId, courseSectionId);
            }
        } else {
            if (skipParentOccurrence) {
                count = jdbc.queryForObject(overlapQuery + parentCondition
                                + " and (s.room_id = ? or s.employee_id = ? or s.course_section_id = ?)",
                        Integer.class, date.getDayOfWeek().getValue(), Date.valueOf(date), Date.valueOf(date),
                        endPeriod, startPeriod, Date.valueOf(date), scheduleId, roomId, employeeId, courseSectionId);
            } else {
                count = jdbc.queryForObject(overlapQuery + " and (s.room_id = ? or s.employee_id = ? or s.course_section_id = ?)",
                        Integer.class, date.getDayOfWeek().getValue(), Date.valueOf(date), Date.valueOf(date),
                        endPeriod, startPeriod, Date.valueOf(date), roomId, employeeId, courseSectionId);
            }
        }
        String exceptionConflictQuery = """
                select count(*) from schedule_exceptions x
                join schedules s on s.id = x.schedule_id and s.deleted_at is null
                where x.deleted_at is null and x.exception_type in ('MAKEUP', 'SUBSTITUTE')
                  and (case when x.exception_type = 'MAKEUP' then x.replacement_date else x.occurrence_date end) = ?
                  and x.start_period <= ? and x.end_period >= ?
                  and not (x.schedule_id = ? and x.occurrence_date = ?)
                """;
        Integer exceptionCount;
        if (employeeId == null) {
            exceptionCount = jdbc.queryForObject(exceptionConflictQuery
                            + " and (x.room_id = ? or s.course_section_id = ?)",
                    Integer.class, Date.valueOf(date), endPeriod, startPeriod,
                    scheduleId, Date.valueOf(originalDate), roomId, courseSectionId);
        } else {
            exceptionCount = jdbc.queryForObject(exceptionConflictQuery
                            + " and (x.room_id = ? or x.employee_id = ? or s.course_section_id = ?)",
                    Integer.class, Date.valueOf(date), endPeriod, startPeriod,
                    scheduleId, Date.valueOf(originalDate), roomId, employeeId, courseSectionId);
        }
        if ((count != null && count > 0) || (exceptionCount != null && exceptionCount > 0)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Phòng hoặc giảng viên đã có lịch trùng ngày và tiết học");
        }
    }

    private void validateRoom(UUID roomId) {
        Integer count = jdbc.queryForObject("""
                select count(*) from rooms
                where id = ? and deleted_at is null and coalesce(is_active, 1) = 1
                  and (status is null or upper(status) not in ('MAINTENANCE', 'INACTIVE', 'UNAVAILABLE'))
                """, Integer.class, roomId);
        if (count == null || count == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phòng học ngoại lệ không khả dụng");
        }
    }

    private void validateEmployee(UUID employeeId) {
        if (employeeId == null) return;
        Integer count = jdbc.queryForObject("""
                select count(*) from employees
                where id = ? and deleted_at is null and coalesce(is_active, 1) = 1
                """, Integer.class, employeeId);
        if (count == null || count == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên được chọn");
        }
    }

    private ScheduleExceptionResponse response(ExceptionRow row) {
        return new ScheduleExceptionResponse(row.id(), row.scheduleId(), row.occurrenceDate(),
                row.type(), row.replacementDate(), row.roomId(), row.employeeId(),
                row.startPeriod(), row.endPeriod(), row.note(), scheduleService.get(row.scheduleId()));
    }

    private LocalDate localDate(Date value) {
        return value == null ? null : value.toLocalDate();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private record ExceptionRow(
            UUID id, UUID scheduleId, LocalDate occurrenceDate, String type,
            LocalDate replacementDate, UUID roomId, UUID employeeId,
            Integer startPeriod, Integer endPeriod, String note) {
    }
}
