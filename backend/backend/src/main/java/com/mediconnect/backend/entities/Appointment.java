package com.mediconnect.backend.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int appointmentId;
    private AppointmentStatus appointmentStatus;
    private LocalDateTime appointmentDate;
    private String appointmentReason;
    private String appointmentTime;
    private String patientId;
    private String doctorId;

    public Appointment(int appointmentId, AppointmentStatus appointmentStatus, LocalDateTime appointmentDate, String appointmentReason, String appointmentTime, String patientId, String doctorId) {
        this.appointmentId = appointmentId;
        this.appointmentStatus = appointmentStatus;
        this.appointmentDate = appointmentDate;
        this.appointmentReason = appointmentReason;
        this.appointmentTime = appointmentTime;
        this.patientId = patientId;
        this.doctorId = doctorId;
    }
    public Appointment (){}

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {this.appointmentId = appointmentId;}

    public AppointmentStatus getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(LocalDateTime appointmentStatus) {
        this.appointmentDate = appointmentStatus;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentReason() {
        return appointmentReason;
    }

    public void setAppointmentReason(String appointmentReason) {
        this.appointmentReason = appointmentReason;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

}
