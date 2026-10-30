package com.example.quanlydaotao.n7.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n7.entity.CourseSection;
import jakarta.persistence.EntityManager;

@Service
@Transactional(readOnly = true)
public class CourseSectionService {

    private final EntityManager entityManager;

    public CourseSectionService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public CourseSection find(UUID id) {
        CourseSection section = entityManager.find(CourseSection.class, id);
        if (section == null || section.getDeletedAt() != null || Boolean.FALSE.equals(section.getActive())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học phần");
        }
        return section;
    }
}
