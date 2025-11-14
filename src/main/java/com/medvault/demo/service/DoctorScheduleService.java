package com.medvault.demo.service;

import com.medvault.demo.entity.Doctor;
import com.medvault.demo.entity.DoctorSchedule;
import com.medvault.demo.repository.AppointmentRepository;
import com.medvault.demo.repository.DoctorScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorScheduleService(DoctorScheduleRepository scheduleRepository,
                                AppointmentRepository appointmentRepository) {
        this.scheduleRepository = scheduleRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Create or update a schedule for a doctor on a specific date
     */
    @Transactional
    public DoctorSchedule createOrUpdateSchedule(Doctor doctor, LocalDate date,
                                                 LocalTime startTime, LocalTime endTime) {

        // Validate inputs
        if (date == null || startTime == null || endTime == null) {
            throw new RuntimeException("Date and times cannot be null");
        }

        // Validate date is not in the past
        if (date.isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot create schedule for past dates");
        }

        // Validate times
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new RuntimeException("Start time must be before end time");
        }

        // Validate minimum duration (at least 1 hour)
        if (startTime.plusHours(1).isAfter(endTime)) {
            throw new RuntimeException("Schedule must be at least 1 hour long");
        }

        // Check if schedule already exists for this date
        Optional<DoctorSchedule> existingSchedule = scheduleRepository
                .findByDoctorAndScheduleDateAndIsActiveTrue(doctor, date);

        if (existingSchedule.isPresent()) {
            // Check if there are any scheduled appointments
            Long appointmentCount = appointmentRepository
                    .countScheduledAppointmentsByDoctorAndDate(doctor.getId(), date);

            if (appointmentCount > 0) {
                throw new RuntimeException(
                        "Cannot modify schedule. There are " + appointmentCount +
                                " scheduled appointments on this date. Please cancel them first."
                );
            }

            // Update existing schedule
            DoctorSchedule schedule = existingSchedule.get();
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);
            return scheduleRepository.save(schedule);
        } else {
            // Create new schedule
            DoctorSchedule schedule = new DoctorSchedule(doctor, date, startTime, endTime);
            return scheduleRepository.save(schedule);
        }
    }

    /**
     * Create multiple schedules (bulk creation for a date range)
     */
    @Transactional
    public List<DoctorSchedule> createSchedulesForDateRange(
            Doctor doctor, LocalDate startDate, LocalDate endDate,
            LocalTime startTime, LocalTime endTime) {

        List<DoctorSchedule> createdSchedules = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            try {
                // Skip if schedule already exists
                if (!scheduleRepository.existsByDoctorIdAndScheduleDateAndIsActiveTrue(
                        doctor.getId(), currentDate)) {
                    DoctorSchedule schedule = createOrUpdateSchedule(
                            doctor, currentDate, startTime, endTime
                    );
                    createdSchedules.add(schedule);
                }
            } catch (Exception e) {
                // Log and continue with next date
                System.err.println("Failed to create schedule for " + currentDate + ": " + e.getMessage());
            }
            currentDate = currentDate.plusDays(1);
        }

        return createdSchedules;
    }

    /**
     * Create recurring schedules for specific days of week
     */
    @Transactional
    public List<DoctorSchedule> createRecurringSchedules(
            Doctor doctor, LocalDate startDate, LocalDate endDate,
            List<DayOfWeek> daysOfWeek, LocalTime startTime, LocalTime endTime) {

        List<DoctorSchedule> createdSchedules = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // Check if current day is in the selected days
            if (daysOfWeek.contains(currentDate.getDayOfWeek())) {
                try {
                    if (!scheduleRepository.existsByDoctorIdAndScheduleDateAndIsActiveTrue(
                            doctor.getId(), currentDate)) {
                        DoctorSchedule schedule = createOrUpdateSchedule(
                                doctor, currentDate, startTime, endTime
                        );
                        createdSchedules.add(schedule);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to create schedule for " + currentDate + ": " + e.getMessage());
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        return createdSchedules;
    }

    /**
     * Get doctor's schedule for a specific date
     */
    public Optional<DoctorSchedule> getScheduleForDate(Doctor doctor, LocalDate date) {
        return scheduleRepository.findByDoctorAndScheduleDateAndIsActiveTrue(doctor, date);
    }

    /**
     * Get all active schedules for a doctor
     */
    public List<DoctorSchedule> getAllActiveSchedules(Doctor doctor) {
        return scheduleRepository.findByDoctorAndIsActiveTrue(doctor);
    }

    /**
     * Get upcoming schedules for a doctor
     */
    public List<DoctorSchedule> getUpcomingSchedules(Doctor doctor) {
        return scheduleRepository.findUpcomingSchedulesByDoctor(doctor, LocalDate.now());
    }

    /**
     * Get schedules for a date range
     */
    public List<DoctorSchedule> getSchedulesForDateRange(
            Doctor doctor, LocalDate startDate, LocalDate endDate) {
        return scheduleRepository.findByDoctorAndDateRange(doctor, startDate, endDate);
    }

    /**
     * Get past schedules for a doctor
     */
    public List<DoctorSchedule> getPastSchedules(Doctor doctor) {
        return scheduleRepository.findPastSchedulesByDoctor(doctor, LocalDate.now());
    }

    /**
     * Delete a schedule (soft delete)
     */
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        // Check if there are any scheduled appointments
        Long appointmentCount = appointmentRepository
                .countScheduledAppointmentsByDoctorAndDate(
                        schedule.getDoctor().getId(),
                        schedule.getScheduleDate()
                );

        if (appointmentCount > 0) {
            throw new RuntimeException(
                    "Cannot delete schedule. There are " + appointmentCount +
                            " scheduled appointments. Please cancel them first."
            );
        }

        schedule.setActive(false); // Soft delete
        scheduleRepository.save(schedule);
    }

    /**
     * Delete schedule by doctor (with ownership check)
     */
    @Transactional
    public void deleteScheduleByDoctor(Long scheduleId, Doctor doctor) {
        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        // Verify ownership
        if (!schedule.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("You can only delete your own schedules");
        }

        deleteSchedule(scheduleId);
    }

    /**
     * Delete multiple schedules for a date range
     */
    @Transactional
    public void deleteSchedulesForDateRange(Doctor doctor, LocalDate startDate, LocalDate endDate) {
        List<DoctorSchedule> schedules = scheduleRepository
                .findByDoctorAndDateRange(doctor, startDate, endDate);

        for (DoctorSchedule schedule : schedules) {
            try {
                deleteSchedule(schedule.getId());
            } catch (Exception e) {
                System.err.println("Failed to delete schedule for " +
                        schedule.getScheduleDate() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Reactivate a deleted schedule
     */
    @Transactional
    public void reactivateSchedule(Long scheduleId, Doctor doctor) {
        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        // Verify ownership
        if (!schedule.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("You can only reactivate your own schedules");
        }

        if (schedule.isActive()) {
            throw new RuntimeException("Schedule is already active");
        }

        // Validate date is not in the past
        if (schedule.getScheduleDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot reactivate schedule for past dates");
        }

        schedule.setActive(true);
        scheduleRepository.save(schedule);
    }

    /**
     * Find schedule by ID
     */
    public DoctorSchedule findById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
    }

    /**
     * Check if doctor has schedule on a specific date
     */
    public boolean hasScheduleOnDate(Doctor doctor, LocalDate date) {
        return scheduleRepository.existsByDoctorIdAndScheduleDateAndIsActiveTrue(
                doctor.getId(), date
        );
    }

    /**
     * Get all doctors with schedules on a specific date
     */
    public List<Doctor> getDoctorsAvailableOnDate(LocalDate date) {
        return scheduleRepository.findDoctorsWithScheduleOnDate(date);
    }

    /**
     * Find next available schedule for a doctor from today
     */
    public Optional<DoctorSchedule> findNextAvailableSchedule(Doctor doctor) {
        List<DoctorSchedule> schedules = scheduleRepository
                .findNextAvailableSchedule(doctor.getId(), LocalDate.now());

        return schedules.isEmpty() ? Optional.empty() : Optional.of(schedules.get(0));
    }

    /**
     * Count active schedules for a doctor
     */
    public Long countActiveSchedules(Doctor doctor) {
        return scheduleRepository.countActiveSchedulesByDoctor(doctor);
    }

    /**
     * Validate schedule doesn't overlap with existing schedules
     * (In case you want to prevent same-day multiple schedules in future)
     */
    private boolean hasOverlappingSchedule(Doctor doctor, LocalDate date,
                                           LocalTime startTime, LocalTime endTime) {
        List<DoctorSchedule> existingSchedules = scheduleRepository
                .findByDoctorAndScheduleDate(doctor, date);

        for (DoctorSchedule existing : existingSchedules) {
            if (existing.isActive()) {
                // Check for time overlap
                if (!(endTime.isBefore(existing.getStartTime()) ||
                        endTime.equals(existing.getStartTime()) ||
                        startTime.isAfter(existing.getEndTime()) ||
                        startTime.equals(existing.getEndTime()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get schedule statistics for doctor dashboard
     */
    public ScheduleStats getScheduleStats(Doctor doctor) {
        Long totalSchedules = countActiveSchedules(doctor);
        List<DoctorSchedule> upcoming = getUpcomingSchedules(doctor);

        return new ScheduleStats(
                totalSchedules,
                (long) upcoming.size(),
                upcoming.isEmpty() ? null : upcoming.get(0).getScheduleDate()
        );
    }

    /**
     * Inner class for statistics
     */
    public static class ScheduleStats {
        private Long totalSchedules;
        private Long upcomingSchedules;
        private LocalDate nextScheduleDate;

        public ScheduleStats(Long totalSchedules, Long upcomingSchedules, LocalDate nextScheduleDate) {
            this.totalSchedules = totalSchedules;
            this.upcomingSchedules = upcomingSchedules;
            this.nextScheduleDate = nextScheduleDate;
        }

        // Getters
        public Long getTotalSchedules() { return totalSchedules; }
        public Long getUpcomingSchedules() { return upcomingSchedules; }
        public LocalDate getNextScheduleDate() { return nextScheduleDate; }
    }
}