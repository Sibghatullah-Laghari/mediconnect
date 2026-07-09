package com.mediconnect.security;

import com.mediconnect.model.Role;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Implementation of Spring Security's UserDetails representing an authenticated user.
 * <p>
 * This class holds the user's core authentication information: ID, email, password hash,
 * role, email verification status, and granted authorities. It is used by Spring Security
 * for authentication and authorization decisions. The {@code isEnabled()} method returns
 * {@code true} only if the user's email is verified, allowing unverified users to be
 * prevented from accessing protected resources.
 * </p>
 */
@Getter
public class AuthenticatedUser implements UserDetails {

    private final Long id;                          // Unique identifier of the user
    private final String email;                     // User's email (used as username)
    private final String password;                  // Hashed password
    private final Role role;                        // User's role (ADMIN, DOCTOR, PATIENT)
    private final boolean emailVerified;            // Whether the user's email has been verified
    private final List<GrantedAuthority> authorities; // Granted authorities based on the role

    /**
     * Constructs an AuthenticatedUser with the given user details.
     * <p>
     * The authorities are automatically derived from the role as a single
     * {@link SimpleGrantedAuthority} with the prefix "ROLE_".
     * </p>
     *
     * @param id             the user ID
     * @param email          the user's email
     * @param password       the hashed password
     * @param role           the user's role
     * @param emailVerified  whether the email is verified
     */
    public AuthenticatedUser(Long id, String email, String password, Role role, boolean emailVerified) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.emailVerified = emailVerified;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Returns the authorities granted to the user.
     *
     * @return a collection of GrantedAuthority objects
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the user's password (hashed).
     *
     * @return the password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the user's email as the username.
     *
     * @return the email
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indicates whether the user's account is non‑expired.
     * Always returns {@code true} (accounts do not expire).
     *
     * @return true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is non‑locked.
     * Always returns {@code true} (locking is handled separately via AccountLockoutService).
     *
     * @return true
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials are non‑expired.
     * Always returns {@code true} (credentials do not expire).
     *
     * @return true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled.
     * <p>
     * A user is considered enabled only if their email has been verified.
     * This prevents unverified users from accessing protected endpoints.
     * </p>
     *
     * @return true if the user's email is verified, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return emailVerified;
    }
}