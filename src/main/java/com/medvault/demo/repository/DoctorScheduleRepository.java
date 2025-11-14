package com.medvault.demo.repository;

import com.medvault.demo.entity.Doctor;
import com.medvault.demo.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    /**
     * Find schedules by doctor and specific date
     */
    List<DoctorSchedule> findByDoctorAndScheduleDate(Doctor doctor, LocalDate scheduleDate);

    /**
     * Find all schedules for a doctor
     */
    List<DoctorSchedule> findByDoctor(Doctor doctor);

    /**
     * Find active schedule for a doctor on a specific date
     */
    Optional<DoctorSchedule> findByDoctorAndScheduleDateAndIsActiveTrue(
            Doctor doctor,
            LocalDate scheduleDate
    );

    /**
     * Find active schedules by doctor ID and date
     */
    List<DoctorSchedule> findByDoctorIdAndScheduleDateAndIsActiveTrue(
            Long doctorId,
            LocalDate scheduleDate
    );

    /**
     * Find all active schedules for a doctor
     */
    List<DoctorSchedule> findByDoctorAndIsActiveTrue(Doctor doctor);

    /**
     * Find all active schedules for a doctor ID
     */
    List<DoctorSchedule> findByDoctorIdAndIsActiveTrue(Long doctorId);

    /**
     * Find schedules within a date range for a doctor
     */
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor = :doctor " +
            "AND ds.scheduleDate BETWEEN :startDate AND :endDate " +
            "AND ds.isActive = true " +
            "ORDER BY ds.scheduleDate ASC")
    List<DoctorSchedule> findByDoctorAndDateRange(
            @Param("doctor") Doctor doctor,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find upcoming schedules for a doctor (from today onwards)
     */
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor = :doctor " +
            "AND ds.scheduleDate >= :currentDate " +
            "AND ds.isActive = true " +
            "ORDER BY ds.scheduleDate ASC")
    List<DoctorSchedule> findUpcomingSchedulesByDoctor(
            @Param("doctor") Doctor doctor,
            @Param("currentDate") LocalDate currentDate
    );

    /**
     * Find past schedules for a doctor
     */
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor = :doctor " +
            "AND ds.scheduleDate < :currentDate " +
            "ORDER BY ds.scheduleDate DESC")
    List<DoctorSchedule> findPastSchedulesByDoctor(
            @Param("doctor") Doctor doctor,
            @Param("currentDate") LocalDate currentDate
    );

    /**
     * Check if doctor has any active schedule for a specific date
     */
    @Query("SELECT CASE WHEN COUNT(ds) > 0 THEN true ELSE false END " +
            "FROM DoctorSchedule ds WHERE ds.doctor.id = :doctorId " +
            "AND ds.scheduleDate = :date " +
            "AND ds.isActive = true")
    boolean existsByDoctorIdAndScheduleDateAndIsActiveTrue(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date
    );

    /**
     * Check if doctor has schedule on a specific date (active or inactive)
     */
    boolean existsByDoctorAndScheduleDate(Doctor doctor, LocalDate scheduleDate);

    /**
     * Delete all schedules for a doctor on a specific date
     */
    void deleteByDoctorAndScheduleDate(Doctor doctor, LocalDate scheduleDate);

    /**
     * Count active schedules for a doctor
     */
    @Query("SELECT COUNT(ds) FROM DoctorSchedule ds WHERE ds.doctor = :doctor " +
            "AND ds.isActive = true")
    Long countActiveSchedulesByDoctor(@Param("doctor") Doctor doctor);

    /**
     * Find all doctors who have schedules on a specific date
     */
    @Query("SELECT DISTINCT ds.doctor FROM DoctorSchedule ds " +
            "WHERE ds.scheduleDate = :date " +
            "AND ds.isActive = true")
    List<Doctor> findDoctorsWithScheduleOnDate(@Param("date") LocalDate date);

    /**
     * Find schedules by doctor ID within date range
     */
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor.id = :doctorId " +
            "AND ds.scheduleDate BETWEEN :startDate AND :endDate " +
            "AND ds.isActive = true " +
            "ORDER BY ds.scheduleDate ASC")
    List<DoctorSchedule> findByDoctorIdAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find next available schedule for a doctor from a given date
     */
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor.id = :doctorId " +
            "AND ds.scheduleDate >= :fromDate " +
            "AND ds.isActive = true " +
            "ORDER BY ds.scheduleDate ASC")
    List<DoctorSchedule> findNextAvailableSchedule(
            @Param("doctorId") Long doctorId,
            @Param("fromDate") LocalDate fromDate
    );

    /**
     * Soft delete - mark schedule as inactive
     */
    @Modifying
    @Transactional
    @Query("UPDATE DoctorSchedule ds SET ds.isActive = false " +
            "WHERE ds.id = :scheduleId")
    void softDeleteById(@Param("scheduleId") Long scheduleId);

    /**
     * Reactivate a schedule
     */
    @Modifying
    @Transactional
    @Query("UPDATE DoctorSchedule ds SET ds.isActive = true " +
            "WHERE ds.id = :scheduleId")
    void reactivateSchedule(@Param("scheduleId") Long scheduleId);
}