package com.example.quanlydaotao.n6.controller;

import com.example.quanlydaotao.n6.dto.*;
import com.example.quanlydaotao.n6.entity.*;
import com.example.quanlydaotao.n6.service.N6Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/n6")
public class N6Controller {
    private final N6Service service;
    public N6Controller(N6Service service) { this.service = service; }

    @GetMapping("/periods") public List<RegistrationPeriod> periods() { return service.periods(); }
    @PostMapping("/periods") @ResponseStatus(HttpStatus.CREATED)
    public RegistrationPeriod createPeriod(@RequestBody PeriodRequest request) { return service.savePeriod(null, request); }
    @PutMapping("/periods/{id}")
    public RegistrationPeriod updatePeriod(@PathVariable UUID id, @RequestBody PeriodRequest request) {
        return service.savePeriod(id, request);
    }
    @DeleteMapping("/periods/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePeriod(@PathVariable UUID id) { service.deletePeriod(id); }

    @GetMapping("/registrations") public List<CourseRegistration> registrations() { return service.registrations(); }
    @PostMapping("/registrations") @ResponseStatus(HttpStatus.CREATED)
    public CourseRegistration register(@RequestBody RegisterRequest request) { return service.register(request); }
    @PatchMapping("/registrations/{id}/cancel")
    public CourseRegistration cancel(@PathVariable UUID id) { return service.cancel(id); }

    @GetMapping("/equivalences") public List<CourseEquivalence> equivalences() { return service.equivalences(); }
    @PostMapping("/equivalences") @ResponseStatus(HttpStatus.CREATED)
    public CourseEquivalence createEquivalence(@RequestBody EquivalenceRequest request) {
        return service.saveEquivalence(null, request);
    }
    @PutMapping("/equivalences/{id}")
    public CourseEquivalence updateEquivalence(@PathVariable UUID id, @RequestBody EquivalenceRequest request) {
        return service.saveEquivalence(id, request);
    }
    @DeleteMapping("/equivalences/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEquivalence(@PathVariable UUID id) { service.deleteEquivalence(id); }

    @GetMapping("/semesters") public List<Map<String,Object>> semesters() { return service.semesters(); }
    @GetMapping("/classes") public List<Map<String,Object>> classes() { return service.classes(); }
    @GetMapping("/courses") public List<Map<String,Object>> courses() { return service.courses(); }
    @GetMapping("/students") public List<Map<String,Object>> students() { return service.students(); }
}
