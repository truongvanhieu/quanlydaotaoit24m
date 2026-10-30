package com.example.quanlydaotao.n7.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.quanlydaotao.n7.dto.request.BuildingRequest;
import com.example.quanlydaotao.n7.dto.response.BuildingResponse;
import com.example.quanlydaotao.n7.dto.response.RoomResponse;
import com.example.quanlydaotao.n7.entity.Building;
import com.example.quanlydaotao.n7.repository.BuildingRepository;
import com.example.quanlydaotao.n7.repository.RoomRepository;

@Service
@Transactional(readOnly = true)
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;

    public BuildingService(
            BuildingRepository buildingRepository,
            RoomRepository roomRepository) {
        this.buildingRepository = buildingRepository;
        this.roomRepository = roomRepository;
    }

    public Page<BuildingResponse> list(String keyword, Boolean active, Pageable pageable) {
        return buildingRepository.search(blankToNull(keyword), active, pageable).map(this::toResponse);
    }

    public BuildingResponse get(UUID id) {
        return toResponse(find(id));
    }

    public List<RoomResponse> rooms(UUID id) {
        find(id);
        return roomRepository.findByBuilding_IdAndDeletedAtIsNullOrderByRoomCode(id)
                .stream()
                .map(RoomService::toResponse)
                .toList();
    }

    @Transactional
    public BuildingResponse create(BuildingRequest request) {
        validate(request);
        String code = request.code().trim();
        if (buildingRepository.existsByCodeIgnoreCaseAndDeletedAtIsNull(code)) {
            throw conflict("Mã tòa nhà đã tồn tại");
        }

        Building building = new Building();
        apply(building, request);
        if (request.active() == null) {
            building.setActive(true);
        }
        return toResponse(buildingRepository.save(building));
    }

    @Transactional
    public BuildingResponse update(UUID id, BuildingRequest request) {
        validate(request);
        Building building = find(id);
        String code = request.code().trim();
        if (buildingRepository.existsByCodeIgnoreCaseAndIdNotAndDeletedAtIsNull(code, id)) {
            throw conflict("Mã tòa nhà đã tồn tại");
        }

        apply(building, request);
        if (request.active() != null) {
            building.setActive(request.active());
        }
        return toResponse(buildingRepository.save(building));
    }

    @Transactional
    public void delete(UUID id) {
        Building building = find(id);
        if (roomRepository.existsByBuilding_IdAndDeletedAtIsNull(id)) {
            throw conflict("Không thể xóa tòa nhà đang có phòng chưa xóa");
        }
        building.setDeletedAt(LocalDateTime.now());
        building.setActive(false);
        buildingRepository.save(building);
    }

    Building find(UUID id) {
        return buildingRepository.findById(id)
                .filter(building -> building.getDeletedAt() == null)
                .orElseThrow(() -> notFound("Không tìm thấy tòa nhà"));
    }

    private BuildingResponse toResponse(Building building) {
        return new BuildingResponse(
                building.getId(),
                building.getCode(),
                building.getName(),
                building.getAddress(),
                building.getTotalFloors(),
                building.getBuildingType(),
                building.getDescription(),
                building.getNote(),
                building.getActive(),
                building.getCreatedAt(),
                building.getUpdatedAt());
    }

    private void apply(Building building, BuildingRequest request) {
        building.setCode(request.code().trim());
        building.setName(request.name().trim());
        building.setAddress(trimToNull(request.address()));
        building.setTotalFloors(request.totalFloors());
        building.setBuildingType(trimToNull(request.buildingType()));
        building.setDescription(trimToNull(request.description()));
        building.setNote(trimToNull(request.note()));
    }

    private void validate(BuildingRequest request) {
        if (request == null || blankToNull(request.code()) == null || blankToNull(request.name()) == null) {
            throw badRequest("Mã và tên tòa nhà là bắt buộc");
        }
        checkLength(request.code(), 100, "Mã tòa nhà");
        checkLength(request.name(), 100, "Tên tòa nhà");
        checkLength(request.address(), 200, "Địa chỉ");
        checkLength(request.buildingType(), 100, "Loại tòa nhà");
        checkLength(request.description(), 255, "Mô tả");
        checkLength(request.note(), 255, "Ghi chú");
        if (request.totalFloors() != null && (request.totalFloors() < 0 || request.totalFloors() > 255)) {
            throw badRequest("Số tầng phải nằm trong khoảng 0 đến 255");
        }
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
