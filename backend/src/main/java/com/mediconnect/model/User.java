package com.mediconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

	@Setter
	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Setter
	@Column(nullable = false)
	private String passwordHash;

	@Setter
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;

	@Setter
	private String refreshToken;

	@Setter
	@Column(nullable = false)
	private boolean emailVerified = false;

	@Setter
	private String verificationCode;

	@Setter
	@Column(nullable = false)
	private int loginAttempts = 0;

	@Setter
	private LocalDateTime lastFailedLoginTime;

	@Setter
	@Column(nullable = false)
	private boolean accountLocked = false;
}
