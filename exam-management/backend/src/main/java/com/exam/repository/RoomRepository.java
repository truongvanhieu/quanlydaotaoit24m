package com.exam.repository;

import com.exam.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findByIsActiveTrueOrderByRoomNumberAsc();
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT r FROM Room r WHERE r.id = :id AND r.isActive = true")
    java.util.Optional<Room> lockActiveById(@org.springframework.data.repository.query.Param("id") UUID id);
}
