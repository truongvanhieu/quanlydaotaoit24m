package com.example.quanlydaotao.n2.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quanlydaotao.n2.entity.AdvisorClasseSection;
import com.example.quanlydaotao.n2.repository.AdvisorClasseSectionRepository;

@Service
@Transactional
public class AdvisorClasseSectionService {

    private final AdvisorClasseSectionRepository repository;

    public AdvisorClasseSectionService(AdvisorClasseSectionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AdvisorClasseSection> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public AdvisorClasseSection getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phân công CVHT với id: " + id));
    }

    @Transactional(readOnly = true)
    public List<AdvisorClasseSection> getByEmployeeId(UUID employeeId) {
        return repository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public List<AdvisorClasseSection> getByStudentClasseId(UUID classeId) {
        return repository.findByStudentClasseId(classeId);
    }

    public AdvisorClasseSection save(AdvisorClasseSection advisorSection) {
        return repository.save(advisorSection);
    }

    public void delete(UUID id) {
        AdvisorClasseSection advisorSection = getById(id);
        advisorSection.setIsActive(false);
        advisorSection.setDeletedAt(LocalDateTime.now());
        repository.save(advisorSection);
    }

    public void hardDelete(UUID id) {
        repository.deleteById(id);
    }
}
