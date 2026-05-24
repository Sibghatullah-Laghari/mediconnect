package com.mediconnect.backend.entities;

public class Doctor {
    private String doctorName;
    protected String doctorId;
    private String doctorGender;
    private String specialization;
    private String doctorPhone;
    private String doctorEmail;
    private String doctorFee;
    private String doctorExperience;

    public Doctor(String doctorName, String doctorId, String doctorGender, String specialization, String doctorPhone, String doctorEmail, String doctorFee, String doctorExperience) {
        this.doctorName = doctorName;
        this.doctorId = doctorId;
        this.doctorGender = doctorGender;
        this.specialization = specialization;
        this.doctorPhone = doctorPhone;
        this.doctorEmail = doctorEmail;
        this.doctorFee = doctorFee;
        this.doctorExperience = doctorExperience;
    }


    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorGender() {
        return doctorGender;
    }

    public void setDoctorGender(String doctorGender) {
        this.doctorGender = doctorGender;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getDoctorPhone() {
        return doctorPhone;
    }

    public void setDoctorPhone(String doctorPhone) {
        this.doctorPhone = doctorPhone;
    }

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }

    public String getDoctorFee() {
        return doctorFee;
    }

    public void setDoctorFee(String doctorFee) {
        this.doctorFee = doctorFee;
    }

    public String getDoctorExperience() {
        return doctorExperience;
    }

    public void setDoctorExperience(String doctorExperience) {
        this.doctorExperience = doctorExperience;
    }

}
