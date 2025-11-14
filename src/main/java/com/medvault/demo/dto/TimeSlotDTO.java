package com.medvault.demo.dto;

import java.time.LocalTime;

/**
 * Data Transfer Object for Time Slots
 * Used to return available appointment slots to patients
 */
public class TimeSlotDTO {

    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isAvailable;
    private String displayTime; // e.g., "09:00 AM - 09:25 AM"

    // Constructors
    public TimeSlotDTO() {}

    public TimeSlotDTO(LocalTime startTime, LocalTime endTime, boolean isAvailable) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = isAvailable;
        this.displayTime = formatTimeRange(startTime, endTime);
    }

    // Helper method to format time range
    private String formatTimeRange(LocalTime start, LocalTime end) {
        return String.format("%s - %s",
                formatTime(start),
                formatTime(end));
    }

    private String formatTime(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();
        String period = hour >= 12 ? "PM" : "AM";

        if (hour > 12) {
            hour -= 12;
        } else if (hour == 0) {
            hour = 12;
        }

        return String.format("%02d:%02d %s", hour, minute, period);
    }

    // Getters & Setters
    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
        if (this.endTime != null) {
            this.displayTime = formatTimeRange(startTime, this.endTime);
        }
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
        if (this.startTime != null) {
            this.displayTime = formatTimeRange(this.startTime, endTime);
        }
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }

    @Override
    public String toString() {
        return "TimeSlotDTO{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", isAvailable=" + isAvailable +
                ", displayTime='" + displayTime + '\'' +
                '}';
    }
}