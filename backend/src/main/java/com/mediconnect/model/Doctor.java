package com.mediconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "doctors")
public class Doctor extends BaseEntity {

    @Setter
    @Column(nullable = false, length = 100)
    private String name;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Setter
    @Column(nullable = false, length = 100)
    private String specialization;

    @Setter
    @Column(nullable = false, length = 20)
    private String phone;

    @Setter
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Setter
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fee;

    @Setter
    @Column(nullable = false)
    private Integer experience;

    @Setter
    @Column(name = "user_id")
    private Long userId;

    @OneToMany(mappedBy = "doctor")
    private List<Appointment> appointments = new ArrayList<>();
}
