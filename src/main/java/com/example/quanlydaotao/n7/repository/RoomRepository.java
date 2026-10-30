package com.example.quanlydaotao.n7.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.quanlydaotao.n7.entity.Room;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    boolean existsByBuilding_IdAndRoomCodeIgnoreCaseAndDeletedAtIsNull(UUID buildingId, String roomCode);

    boolean existsByBuilding_IdAndRoomCodeIgnoreCaseAndIdNotAndDeletedAtIsNull(
            UUID buildingId, String roomCode, UUID id);

    boolean existsByBuilding_IdAndDeletedAtIsNull(UUID buildingId);

    long countByBuilding_IdAndDeletedAtIsNull(UUID buildingId);

    List<Room> findByBuilding_IdAndDeletedAtIsNullOrderByRoomCode(UUID buildingId);

    @Query("""
            select r from Room r
            where r.deletedAt is null
              and (:active is null or r.active = :active)
              and (:buildingId is null or r.building.id = :buildingId)
              and (:status is null or lower(coalesce(r.status, '')) = lower(:status))
              and (:roomType is null or lower(coalesce(r.roomType, '')) = lower(:roomType))
              and (:minCapacity is null or r.capacity >= :minCapacity)
              and (:keyword is null
                   or lower(r.roomCode) like lower(concat('%', :keyword, '%'))
                   or lower(r.roomName) like lower(concat('%', :keyword, '%')))
            """)
    Page<Room> search(
            @Param("keyword") String keyword,
            @Param("buildingId") UUID buildingId,
            @Param("status") String status,
            @Param("roomType") String roomType,
            @Param("minCapacity") Integer minCapacity,
            @Param("active") Boolean active,
            Pageable pageable);
}
