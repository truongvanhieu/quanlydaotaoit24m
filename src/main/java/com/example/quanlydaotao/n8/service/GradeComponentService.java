package com.example.quanlydaotao.n8.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.quanlydaotao.n8.entity.GradeComponent;
import com.example.quanlydaotao.n8.repository.GradeComponentRepository;

@Service
public class GradeComponentService {
    private final GradeComponentRepository repository;

    public GradeComponentService(GradeComponentRepository repository) {
        this.repository = repository;
    }

    public List<GradeComponent> getAll() { return repository.findAll(); }
    public GradeComponent getById(UUID id) { return repository.findById(id).orElse(null); }
    public GradeComponent save(GradeComponent g) { return repository.save(g); }
    public void delete(UUID id) { repository.deleteById(id); }
}