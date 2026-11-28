package com.example.quanlydaotao.n2.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

<<<<<<< HEAD
import com.example.quanlydaotao.n1.security.RequirePermission;
=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
import com.example.quanlydaotao.n2.entity.Student;
import com.example.quanlydaotao.n2.service.StudentService;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
<<<<<<< HEAD
    @RequirePermission("STUDENT_VIEW")
=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
    public List<Student> listStudents(
            @RequestParam(required = false) String keyword) {

        return studentService.search(keyword);
    }

    @GetMapping("/{id}")
<<<<<<< HEAD
    @RequirePermission("STUDENT_VIEW")
=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
    public Student getStudent(@PathVariable UUID id) {

        return studentService.getById(id);
    }

    @PostMapping
<<<<<<< HEAD
    @RequirePermission("STUDENT_CREATE")
=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
    public Student createStudent(@RequestBody Student student) {

        return studentService.save(student);
    }

    @PutMapping("/{id}")
<<<<<<< HEAD
    @RequirePermission("STUDENT_UPDATE")
=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
    public Student updateStudent(
            @PathVariable UUID id,
            @RequestBody Student student) {

        student.setId(id);

        return studentService.save(student);
    }

    @DeleteMapping("/{id}")
<<<<<<< HEAD
    @RequirePermission("STUDENT_DELETE")
=======
>>>>>>> 0d7aa9710788386908febc8ddaa6393808c6dcac
    public void deleteStudent(@PathVariable UUID id) {

        studentService.delete(id);
    }
}