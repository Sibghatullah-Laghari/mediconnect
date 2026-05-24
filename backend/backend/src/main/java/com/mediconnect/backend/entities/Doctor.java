package com.mediconnect.backend.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "doctors")
public class Doctor {

    private String doctorName;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int doctorId;
    private Gender doctorGender;
    private String specialization;
    private String doctorPhone;
    private String doctorEmail;
    private int doctorFee;
    private int doctorExperience;

    public Doctor(String doctorName, int doctorId, Gender doctorGender, String specialization, String doctorPhone, String doctorEmail, int doctorFee, int doctorExperience) {
        this.doctorName = doctorName;
        this.doctorId = doctorId;
        this.doctorGender = doctorGender;
        this.specialization = specialization;
        this.doctorPhone = doctorPhone;
        this.doctorEmail = doctorEmail;
        this.doctorFee = doctorFee;
        this.doctorExperience = doctorExperience;
    }

    public Doctor() {

    }


    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public Gender getDoctorGender() {
        return doctorGender;
    }

    public void setDoctorGender(Gender doctorGender) {
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

    public int getDoctorFee() {
        return doctorFee;
    }

    public void setDoctorFee(int doctorFee) {
        this.doctorFee = doctorFee;
    }

    public int getDoctorExperience() {
        return doctorExperience;
    }

    public void setDoctorExperience(int doctorExperience) {
        this.doctorExperience = doctorExperience;
    }

}