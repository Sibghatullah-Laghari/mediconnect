package com.mediconnect.backend.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "patients")

public class Patient {
    private String patientName;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int patientId;
    private String patientEmail;
    private String patientPhone;
    private int patientAge;
    private Gender patientGender;
    private String patientAddress;

    public Patient(String patientName, int patientId, String patientEmail, String patientPhone, int patientAge, Gender patientGender, String patientAddress) {
        this.patientName = patientName;
        this.patientId = patientId;
        this.patientEmail = patientEmail;
        this.patientPhone = patientPhone;
        this.patientAge = patientAge;
        this.patientGender = patientGender;
        this.patientAddress = patientAddress;
    }

    public Patient(){};


    public String getPatientName() {
        return patientName;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public int getPatientAge() {
        return patientAge;
    }

    public Gender getPatientGender() {
        return patientGender;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public void setPatientAge(int patientAge) {
        this.patientAge = patientAge;
    }

    public void setPatientGender(Gender patientGender) {
        this.patientGender = patientGender;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }
}
