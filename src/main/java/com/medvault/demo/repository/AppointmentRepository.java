package com.medvault.demo.repository;

import com.medvault.demo.entity.Appointment;
import com.medvault.demo.entity.AppointmentStatus;
import com.medvault.demo.entity.Doctor;
import com.medvault.demo.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Find all appointments for a specific doctor on a specific date
     */
    List<Appointment> findByDoctorAndAppointmentDate(Doctor doctor, LocalDate appointmentDate);

    /**
     * Find all appointments for a specific patient
     */
    List<Appointment> findByPatient(Patient patient);

    /**
     * Find all appointments for a specific doctor
     */
    List<Appointment> findByDoctor(Doctor doctor);

    /**
     * Find appointments by doctor, date and status
     */
    List<Appointment> findByDoctorAndAppointmentDateAndStatus(
            Doctor doctor,
            LocalDate date,
            AppointmentStatus status
    );

    /**
     * Find appointments by patient and status
     */
    List<Appointment> findByPatientAndStatus(Patient patient, AppointmentStatus status);

    /**
     * Find upcoming appointments for a patient
     */
    @Query("SELECT a FROM Appointment a WHERE a.patient = :patient " +
            "AND a.appointmentDate >= :currentDate " +
            "AND a.status = 'SCHEDULED' " +
            "ORDER BY a.appointmentDate ASC, a.startTime ASC")
    List<Appointment> findUpcomingAppointmentsByPatient(
            @Param("patient") Patient patient,
            @Param("currentDate") LocalDate currentDate
    );

    /**
     * Find upcoming appointments for a doctor
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor " +
            "AND a.appointmentDate >= :currentDate " +
            "AND a.status = 'SCHEDULED' " +
            "ORDER BY a.appointmentDate ASC, a.startTime ASC")
    List<Appointment> findUpcomingAppointmentsByDoctor(
            @Param("doctor") Doctor doctor,
            @Param("currentDate") LocalDate currentDate
    );

    /**
     * Find past appointments for a patient
     */
    @Query("SELECT a FROM Appointment a WHERE a.patient = :patient " +
            "AND a.appointmentDate < :currentDate " +
            "ORDER BY a.appointmentDate DESC, a.startTime DESC")
    List<Appointment> findPastAppointmentsByPatient(
            @Param("patient") Patient patient,
            @Param("currentDate") LocalDate currentDate
    );

    /**
     * Check if there are overlapping appointments for a doctor
     * This is used to prevent double booking
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND a.appointmentDate = :date " +
            "AND a.status = 'SCHEDULED' " +
            "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    List<Appointment> findOverlappingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    /**
     * Count scheduled appointments for a doctor on a specific date
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND a.appointmentDate = :date " +
            "AND a.status = 'SCHEDULED'")
    Long countScheduledAppointmentsByDoctorAndDate(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date
    );

    /**
     * Find appointment by doctor, patient and date
     */
    Optional<Appointment> findByDoctorAndPatientAndAppointmentDate(
            Doctor doctor,
            Patient patient,
            LocalDate appointmentDate
    );

    /**
     * Find all appointments for a doctor within a date range
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor " +
            "AND a.appointmentDate BETWEEN :startDate AND :endDate " +
            "ORDER BY a.appointmentDate ASC, a.startTime ASC")
    List<Appointment> findByDoctorAndDateRange(
            @Param("doctor") Doctor doctor,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find all appointments for a patient within a date range
     */
    @Query("SELECT a FROM Appointment a WHERE a.patient = :patient " +
            "AND a.appointmentDate BETWEEN :startDate AND :endDate " +
            "ORDER BY a.appointmentDate ASC, a.startTime ASC")
    List<Appointment> findByPatientAndDateRange(
            @Param("patient") Patient patient,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Count total appointments for a doctor (all time)
     */
    Long countByDoctor(Doctor doctor);

    /**
     * Count total appointments for a patient (all time)
     */
    Long countByPatient(Patient patient);

    /**
     * Check if patient has any scheduled appointment with doctor
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Appointment a WHERE a.doctor = :doctor " +
            "AND a.patient = :patient " +
            "AND a.status = 'SCHEDULED'")
    boolean hasScheduledAppointmentWithDoctor(
            @Param("doctor") Doctor doctor,
            @Param("patient") Patient patient
    );
}