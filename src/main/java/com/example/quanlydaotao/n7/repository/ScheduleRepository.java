package com.example.quanlydaotao.n7.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.quanlydaotao.n7.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    boolean existsByRoom_IdAndDeletedAtIsNull(UUID roomId);

    List<Schedule> findByRoom_IdAndDeletedAtIsNullOrderByStartDateAscStartPeriodAsc(UUID roomId);

    List<Schedule> findByCourseSection_IdAndDeletedAtIsNullOrderByDayOfWeekAscStartPeriodAsc(UUID courseSectionId);

    @Query("""
            select s from Schedule s
            left join s.courseSection cs
            left join cs.course c
            left join s.room r
            left join r.building b
            where s.deletedAt is null
              and (:active is null or s.active = :active)
              and (:roomId is null or r.id = :roomId)
              and (:courseSectionId is null or cs.id = :courseSectionId)
              and (:dayOfWeek is null or s.dayOfWeek = :dayOfWeek)
              and (:fromDate is null or s.endDate >= :fromDate)
              and (:toDate is null or s.startDate <= :toDate)
              and (:keyword is null
                   or lower(coalesce(cs.code, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(cs.name, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(c.code, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(c.name, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(r.roomCode, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(b.name, '')) like lower(concat('%', :keyword, '%')))
            """)
    Page<Schedule> search(
            @Param("keyword") String keyword,
            @Param("roomId") UUID roomId,
            @Param("courseSectionId") UUID courseSectionId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("active") Boolean active,
            Pageable pageable);

    @Query("""
            select s from Schedule s
            where s.deletedAt is null
              and s.active = true
              and (:excludeId is null or s.id <> :excludeId)
              and s.dayOfWeek = :dayOfWeek
              and s.startDate <= :endDate
              and s.endDate >= :startDate
              and s.startPeriod <= :endPeriod
              and s.endPeriod >= :startPeriod
              and (s.room.id = :roomId or s.courseSection.id = :courseSectionId)
            """)
    List<Schedule> findPotentialConflicts(
            @Param("excludeId") UUID excludeId,
            @Param("roomId") UUID roomId,
            @Param("courseSectionId") UUID courseSectionId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("startPeriod") Integer startPeriod,
            @Param("endPeriod") Integer endPeriod);

    @Query("""
            select s from Schedule s
            where s.deletedAt is null
              and s.active = true
              and s.dayOfWeek = :dayOfWeek
              and s.startDate <= :dateEnd
              and s.endDate >= :dateStart
              and s.startPeriod <= :endPeriod
              and s.endPeriod >= :startPeriod
            """)
    List<Schedule> findOccupied(
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("dateStart") LocalDateTime dateStart,
            @Param("dateEnd") LocalDateTime dateEnd,
            @Param("startPeriod") Integer startPeriod,
            @Param("endPeriod") Integer endPeriod);

    @Query(value = """
            select distinct s.* from schedules s
            join student_course_sections scs on scs.course_section_id = s.course_section_id
            where scs.student_id = :studentId and scs.deleted_at is null
              and coalesce(scs.is_active, 1) = 1 and s.deleted_at is null
              and coalesce(s.is_active, 1) = 1
            """, nativeQuery = true)
    List<Schedule> findForStudent(@Param("studentId") UUID studentId);

    @Query(value = """
            select s.* from schedules s
            where s.deleted_at is null and coalesce(s.is_active, 1) = 1
              and (s.employee_id = :lecturerId or s.employee_id in (
                    select e.id from lecturers l join employees e on e.user_id = l.user_id
                    where l.id = :lecturerId and l.user_id is not null
                      and l.deleted_at is null and e.deleted_at is null
              ))
            order by s.start_date, s.start_period
            """, nativeQuery = true)
    List<Schedule> findForLecturer(@Param("lecturerId") UUID lecturerId);
}
