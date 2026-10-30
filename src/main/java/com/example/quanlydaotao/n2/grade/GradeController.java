package com.example.quanlydaotao.n2.grade;

import static com.example.quanlydaotao.n2.grade.GradeModels.*;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping("/course-classes")
    public List<CourseClassOption> getCourseClasses() {
        return gradeService.getCourseClasses();
    }

    @GetMapping("/components")
    public List<GradeComponentView> getComponents(@RequestParam UUID courseClassId) {
        return gradeService.getComponents(courseClassId);
    }

    @PostMapping("/components")
    public GradeComponentView createComponent(@RequestBody GradeComponentRequest request) {
        return gradeService.createComponent(request);
    }

    @PutMapping("/components/{id}")
    public GradeComponentView updateComponent(
            @PathVariable UUID id,
            @RequestBody GradeComponentRequest request) {
        return gradeService.updateComponent(id, request);
    }

    @DeleteMapping("/components/{id}")
    public void deleteComponent(@PathVariable UUID id) {
        gradeService.deleteComponent(id);
    }

    @GetMapping
    public GradeBookView getGradeBook(@RequestParam UUID courseClassId) {
        return gradeService.getGradeBook(courseClassId);
    }

    @PostMapping("/entries")
    public GradeEntryView createEntry(@RequestBody GradeEntryRequest request) {
        return gradeService.createEntry(request);
    }

    @PutMapping("/entries/{id}")
    public GradeEntryView updateEntry(
            @PathVariable UUID id,
            @RequestBody GradeEntryUpdateRequest request) {
        return gradeService.updateEntry(id, request);
    }

    @DeleteMapping("/entries/{id}")
    public void deleteEntry(@PathVariable UUID id) {
        gradeService.deleteEntry(id);
    }

}
