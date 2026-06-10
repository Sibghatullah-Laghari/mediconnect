package com.mediconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "patients")
@SQLRestriction("is_deleted = false")
public class Patient extends BaseEntity {

    @Setter
    @Column(nullable = false, length = 100)
    private String name;

    @Setter
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Setter
    @Column(nullable = false, length = 20)
    private String phone;

    @Setter
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Setter
    @Column(length = 500)
    private String address;

    @Setter
    @Column(name = "user_id")
    private Long userId;

    @OneToMany(mappedBy = "patient")
    private List<Appointment> appointments = new ArrayList<>();
}
