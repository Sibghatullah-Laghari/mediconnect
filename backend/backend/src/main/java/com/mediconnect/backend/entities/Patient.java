package com.mediconnect.backend.entities;

public class Patient {
    private String patientName;
    protected String patientId;
    private String patientEmail;
    private String patientPhone;
    private String patientAge;
    private String patientGender;
    private String patientAddress;

    public Patient(String patientName, String patientId, String patientEmail, String patientPhone, String patientAge, String patientGender, String patientAddress) {
        this.patientName = patientName;
        this.patientId = patientId;
        this.patientEmail = patientEmail;
        this.patientPhone = patientPhone;
        this.patientAge = patientAge;
        this.patientGender = patientGender;
        this.patientAddress = patientAddress;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public String getPatientAge() {
        return patientAge;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public void setPatientAge(String patientAge) {
        this.patientAge = patientAge;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }
}
