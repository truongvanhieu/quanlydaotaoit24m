package com.example.quanlydaotao.n7.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.quanlydaotao.n7.entity.Building;
import com.example.quanlydaotao.n7.entity.Room;
import com.example.quanlydaotao.n7.entity.Schedule;
import com.example.quanlydaotao.n7.repository.BuildingRepository;
import com.example.quanlydaotao.n7.repository.RoomRepository;
import com.example.quanlydaotao.n7.repository.ScheduleRepository;

class RoomServiceTest {

    @Test
    void excludesRoomsOccupiedOnAnyRecurringOccurrence() {
        RoomRepository roomRepository = mock(RoomRepository.class);
        ScheduleRepository scheduleRepository = mock(ScheduleRepository.class);
        RoomService service = new RoomService(
                roomRepository,
                scheduleRepository,
                mock(BuildingRepository.class),
                mock(ScheduleService.class));

        Room occupiedRoom = room("B10101");
        Room availableRoom = room("B20203");
        Schedule laterOccurrence = new Schedule();
        laterOccurrence.setRoom(occupiedRoom);
        laterOccurrence.setDayOfWeek(1);
        laterOccurrence.setStartPeriod(1);
        laterOccurrence.setEndPeriod(3);
        laterOccurrence.setStartDate(LocalDateTime.parse("2027-02-08T00:00:00"));
        laterOccurrence.setEndDate(LocalDateTime.parse("2027-02-08T02:30:00"));

        when(scheduleRepository.findOccupiedInRange(
                eq(1),
                eq(LocalDate.parse("2027-02-01").atStartOfDay()),
                any(LocalDateTime.class),
                eq(1),
                eq(5)))
                .thenReturn(List.of(laterOccurrence));
        when(roomRepository.search(
                eq(null), eq(null), eq(null), eq(null), eq(14), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(occupiedRoom, availableRoom)));

        List<String> availableCodes = service.availableForSchedule(
                        LocalDate.parse("2027-02-01"),
                        LocalDate.parse("2027-05-02"),
                        1,
                        1,
                        5,
                        14)
                .stream()
                .map(room -> room.roomCode())
                .toList();

        assertEquals(List.of("B20203"), availableCodes);
    }

    private Room room(String code) {
        Building building = new Building();
        building.setId(UUID.randomUUID());
        building.setCode("B1");
        building.setName("Tòa A");

        Room room = new Room();
        room.setId(UUID.randomUUID());
        room.setBuilding(building);
        room.setRoomCode(code);
        room.setRoomName(code);
        room.setCapacity(50);
        room.setStatus("Đang sử dụng");
        return room;
    }
}
