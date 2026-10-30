package com.example.quanlydaotao.n7.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n7.dto.request.RoomRequest;
import com.example.quanlydaotao.n7.dto.response.RoomResponse;
import com.example.quanlydaotao.n7.dto.response.ScheduleResponse;
import com.example.quanlydaotao.n7.entity.Building;
import com.example.quanlydaotao.n7.entity.Room;
import com.example.quanlydaotao.n7.repository.BuildingRepository;
import com.example.quanlydaotao.n7.repository.RoomRepository;
import com.example.quanlydaotao.n7.repository.ScheduleRepository;

@Service
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final ScheduleRepository scheduleRepository;
    private final BuildingRepository buildingRepository;

    public RoomService(
            RoomRepository roomRepository,
            ScheduleRepository scheduleRepository,
            BuildingRepository buildingRepository) {
        this.roomRepository = roomRepository;
        this.scheduleRepository = scheduleRepository;
        this.buildingRepository = buildingRepository;
    }

    public Page<RoomResponse> list(
            String keyword,
            UUID buildingId,
            String status,
            String roomType,
            Integer minCapacity,
            Boolean active,
            Pageable pageable) {
        if (minCapacity != null && minCapacity < 0) {
            throw badRequest("Sức chứa tối thiểu không được âm");
        }
        return roomRepository.search(
                        blankToNull(keyword),
                        buildingId,
                        blankToNull(status),
                        blankToNull(roomType),
                        minCapacity,
                        active,
                        pageable)
                .map(RoomService::toResponse);
    }

    public RoomResponse get(UUID id) {
        return toResponse(find(id));
    }

    public List<ScheduleResponse> schedules(UUID id) {
        find(id);
        return scheduleRepository.findByRoom_IdAndDeletedAtIsNullOrderByStartDateAscStartPeriodAsc(id)
                .stream()
                .map(ScheduleService::toResponse)
                .toList();
    }

    public List<RoomResponse> available(
            LocalDate date,
            Integer startPeriod,
            Integer endPeriod,
            Integer minCapacity) {
        if (date == null || startPeriod == null || endPeriod == null
                || startPeriod < 1 || endPeriod < startPeriod) {
            throw badRequest("Cần ngày và khoảng tiết học hợp lệ");
        }
        if (minCapacity != null && minCapacity < 0) {
            throw badRequest("Sức chứa tối thiểu không được âm");
        }

        int dayOfWeek = date.getDayOfWeek().getValue();
        LocalDateTime dateStart = date.atStartOfDay();
        LocalDateTime dateEnd = date.atTime(23, 59, 59, 999_999_900);
        Set<UUID> occupiedRoomIds = scheduleRepository.findOccupied(
                        dayOfWeek, dateStart, dateEnd, startPeriod, endPeriod)
                .stream()
                .map(schedule -> schedule.getRoom().getId())
                .collect(Collectors.toSet());

        return roomRepository.search(null, null, null, null, minCapacity, true, Pageable.unpaged())
                .stream()
                .filter(room -> isAvailableStatus(room.getStatus()))
                .filter(room -> !occupiedRoomIds.contains(room.getId()))
                .map(RoomService::toResponse)
                .toList();
    }

    @Transactional
    public RoomResponse create(RoomRequest request) {
        validate(request);
        Building building = findBuilding(request.buildingId());
        String roomCode = request.roomCode().trim();
        if (roomRepository.existsByBuilding_IdAndRoomCodeIgnoreCaseAndDeletedAtIsNull(
                building.getId(), roomCode)) {
            throw conflict("Mã phòng đã tồn tại trong tòa nhà");
        }

        Room room = new Room();
        apply(room, request, building);
        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse update(UUID id, RoomRequest request) {
        validate(request);
        Room room = find(id);
        Building building = findBuilding(request.buildingId());
        String roomCode = request.roomCode().trim();
        if (roomRepository.existsByBuilding_IdAndRoomCodeIgnoreCaseAndIdNotAndDeletedAtIsNull(
                building.getId(), roomCode, id)) {
            throw conflict("Mã phòng đã tồn tại trong tòa nhà");
        }

        apply(room, request, building);
        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public void delete(UUID id) {
        Room room = find(id);
        if (scheduleRepository.existsByRoom_IdAndDeletedAtIsNull(id)) {
            throw conflict("Không thể xóa phòng đang được sử dụng trong lịch học");
        }
        room.setDeletedAt(LocalDateTime.now());
        room.setActive(false);
        roomRepository.save(room);
    }

    Room find(UUID id) {
        return roomRepository.findById(id)
                .filter(room -> room.getDeletedAt() == null)
                .orElseThrow(() -> notFound("Không tìm thấy phòng học"));
    }

    static RoomResponse toResponse(Room room) {
        Building building = room.getBuilding();
        return new RoomResponse(
                room.getId(),
                building.getId(),
                building.getCode(),
                building.getName(),
                room.getRoomCode(),
                room.getRoomName(),
                room.getFloor(),
                room.getCapacity(),
                room.getRoomType(),
                room.getStatus(),
                room.isHasProjector(),
                room.isHasAirConditioner(),
                room.isHasComputer(),
                room.getDescription(),
                room.isActive(),
                room.getCreatedAt(),
                room.getUpdatedAt());
    }

    private Building findBuilding(UUID id) {
        return buildingRepository.findById(id)
                .filter(building -> building.getDeletedAt() == null)
                .orElseThrow(() -> notFound("Không tìm thấy tòa nhà"));
    }

    private void apply(Room room, RoomRequest request, Building building) {
        room.setBuilding(building);
        room.setRoomCode(request.roomCode().trim());
        room.setRoomName(request.roomName().trim());
        room.setFloor(request.floor());
        room.setCapacity(request.capacity());
        room.setRoomType(trimToNull(request.roomType()));
        room.setStatus(request.status().trim());
        room.setHasProjector(Boolean.TRUE.equals(request.hasProjector()));
        room.setHasAirConditioner(Boolean.TRUE.equals(request.hasAirConditioner()));
        room.setHasComputer(Boolean.TRUE.equals(request.hasComputer()));
        room.setDescription(trimToNull(request.description()));
        if (request.active() != null) {
            room.setActive(request.active());
        }
    }

    private void validate(RoomRequest request) {
        if (request == null || request.buildingId() == null
                || blankToNull(request.roomCode()) == null
                || blankToNull(request.roomName()) == null
                || blankToNull(request.status()) == null) {
            throw badRequest("Tòa nhà, mã phòng, tên phòng và trạng thái là bắt buộc");
        }
        checkLength(request.roomCode(), 20, "Mã phòng");
        checkLength(request.roomName(), 100, "Tên phòng");
        checkLength(request.roomType(), 50, "Loại phòng");
        checkLength(request.status(), 50, "Trạng thái");
        checkLength(request.description(), 255, "Mô tả");
        if (request.floor() != null && request.floor() < 0) {
            throw badRequest("Tầng không được âm");
        }
        if (request.capacity() != null && request.capacity() < 0) {
            throw badRequest("Sức chứa không được âm");
        }
    }

    private boolean isAvailableStatus(String status) {
        return status == null || !(status.equalsIgnoreCase("MAINTENANCE")
                || status.equalsIgnoreCase("INACTIVE")
                || status.equalsIgnoreCase("UNAVAILABLE"));
    }

    private void checkLength(String value, int max, String field) {
        if (value != null && value.length() > max) {
            throw badRequest(field + " không được dài quá " + max + " ký tự");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String trimToNull(String value) {
        return value == null ? null : value.trim();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
