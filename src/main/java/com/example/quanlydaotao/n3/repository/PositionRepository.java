
package com.example.quanlydaotao.n3.repository;

import com.example.quanlydaotao.n3.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PositionRepository
        extends JpaRepository<Position, UUID> {
}
