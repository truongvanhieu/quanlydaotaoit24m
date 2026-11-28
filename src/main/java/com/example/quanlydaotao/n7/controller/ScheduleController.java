package com.example.quanlydaotao.n7.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.quanlydaotao.n7.dto.request.ScheduleRequest;
import com.example.quanlydaotao.n7.dto.response.ScheduleResponse;
import com.example.quanlydaotao.n7.dto.response.CourseSectionResponse;
import com.example.quanlydaotao.n7.dto.response.ShiftOption;
import com.example.quanlydaotao.n7.service.ScheduleService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public Page<ScheduleResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) UUID courseSectionId,
            @RequestParam(required = false) UUID semesterId,
            @RequestParam(required = false) Integer dayOfWeek,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = {"startDate", "startPeriod"}) Pageable pageable) {
        return scheduleService.list(
                keyword, roomId, courseSectionId, semesterId, dayOfWeek, fromDate, toDate, year, active, pageable);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) UUID courseSectionId,
            @RequestParam(required = false) UUID semesterId,
            @RequestParam(required = false) Integer dayOfWeek,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer year) throws IOException {
        List<ScheduleResponse> schedules = scheduleService.list(keyword, roomId, courseSectionId, semesterId,
                dayOfWeek, fromDate, toDate, year, true, Pageable.unpaged()).getContent();
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Lịch học");
            String[] headers = { "Mã lớp", "Tên lớp", "Môn học", "Giảng viên", "Phòng", "Tòa nhà",
                    "Thứ", "Ca", "Tiết bắt đầu", "Tiết kết thúc", "Từ ngày", "Đến ngày", "Hình thức", "Trạng thái" };
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            int rowNumber = 1;
            for (ScheduleResponse item : schedules) {
                Row row = sheet.createRow(rowNumber++);
                String[] values = {
                        item.courseSectionCode(), item.courseSectionName(), item.courseName(), item.lecturerName(),
                        item.roomCode(), item.buildingName(), item.dayOfWeek() == null ? "" : item.dayOfWeek().toString(),
                        scheduleService.shiftName(item.shiftId()), item.startPeriod() == null ? "" : item.startPeriod().toString(),
                        item.endPeriod() == null ? "" : item.endPeriod().toString(),
                        item.startDate() == null ? "" : item.startDate().toLocalDate().toString(),
                        item.endDate() == null ? "" : item.endDate().toLocalDate().toString(),
                        item.mode(), item.scheduleStatus() == null ? item.status() : item.scheduleStatus()
                };
                for (int i = 0; i < values.length; i++) row.createCell(i).setCellValue(values[i] == null ? "" : values[i]);
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(output);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"schedules.xlsx\"")
                    .body(output.toByteArray());
        }
    }

    @GetMapping("/options/shifts")
    public List<ShiftOption> shifts() {
        return scheduleService.shifts();
    }

    @GetMapping("/weekly")
    public List<ScheduleResponse> weekly(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.weekly(date);
    }

    @GetMapping("/student/{studentId}/weekly")
    public List<ScheduleResponse> weeklyForStudent(@PathVariable UUID studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.weeklyForStudent(studentId, date);
    }

    @GetMapping("/student/by-code/{studentCode}/weekly")
    public List<ScheduleResponse> weeklyForStudentCode(@PathVariable String studentCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.weeklyForStudentCode(studentCode, date);
    }

    @GetMapping("/lecturer/{lecturerId}/weekly")
    public List<ScheduleResponse> weeklyForLecturer(@PathVariable UUID lecturerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.weeklyForLecturer(lecturerId, date);
    }

    @GetMapping("/lecturer/by-code/{lecturerCode}/weekly")
    public List<ScheduleResponse> weeklyForLecturerCode(@PathVariable String lecturerCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.weeklyForLecturerCode(lecturerCode, date);
    }

    @GetMapping("/student/by-code/{studentCode}/course-sections")
    public List<CourseSectionResponse> courseSectionsForStudent(@PathVariable String studentCode) {
        return scheduleService.courseSectionsForStudentCode(studentCode);
    }

    @GetMapping("/lecturer/by-code/{lecturerCode}/course-sections")
    public List<CourseSectionResponse> courseSectionsForLecturer(@PathVariable String lecturerCode) {
        return scheduleService.courseSectionsForLecturerCode(lecturerCode);
    }

    @PostMapping("/{audience}/by-code/{personCode}")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse createForPerson(@PathVariable String audience, @PathVariable String personCode,
            @RequestBody ScheduleRequest request) {
        return scheduleService.createForPerson(audience, personCode, request);
    }

    @PutMapping("/{audience}/by-code/{personCode}/{id}")
    public ScheduleResponse updateForPerson(@PathVariable String audience, @PathVariable String personCode,
            @PathVariable UUID id, @RequestBody ScheduleRequest request) {
        return scheduleService.updateForPerson(audience, personCode, id, request);
    }

    @DeleteMapping("/{audience}/by-code/{personCode}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteForPerson(@PathVariable String audience, @PathVariable String personCode, @PathVariable UUID id) {
        scheduleService.deleteForPerson(audience, personCode, id);
    }

    @GetMapping("/course-section/{courseSectionId}")
    public List<ScheduleResponse> byCourseSection(@PathVariable UUID courseSectionId) {
        return scheduleService.byCourseSection(courseSectionId);
    }

    @GetMapping("/{id}")
    public ScheduleResponse get(@PathVariable UUID id) {
        return scheduleService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse create(@RequestBody ScheduleRequest request) {
        return scheduleService.create(request);
    }

    @PutMapping("/{id}")
    public ScheduleResponse update(@PathVariable UUID id, @RequestBody ScheduleRequest request) {
        return scheduleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        scheduleService.delete(id);
    }
}
