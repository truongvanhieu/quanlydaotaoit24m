
package com.example.quanlydaotao.n3.repository;

import com.example.quanlydaotao.n3.entity.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LecturerRepository
        extends JpaRepository<Lecturer, UUID> {
}
