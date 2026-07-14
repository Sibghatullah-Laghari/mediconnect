package com.mediconnect.util;

import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.Role;
import com.mediconnect.security.AuthenticatedUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for common security-related operations.
 * <p>
 * Provides static helper methods to access the current authentication context,
 * retrieve the authenticated user's email and ID, verify roles,
 * and enforce role-based authorization. This utility is intended for
 * application-wide security checks and user context retrieval.
 * </p>
 */
public final class SecurityUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private SecurityUtils() {
    }

    /**
     * Returns the current Authentication object from the SecurityContext.
     *
     * @return the current Authentication object, or null if none is available
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Returns the email address of the currently authenticated user.
     *
     * @return the authenticated user's email
     * @throws UnauthorizedException if no authenticated user is available
     */
    public static String getCurrentUserEmail() {
        return getCurrentAuthenticatedUser().getEmail();
    }

    /**
     * Returns the ID of the currently authenticated user.
     *
     * @return the authenticated user's ID
     * @throws UnauthorizedException if no authenticated user is available
     */
    public static Long getCurrentUserId() {
        return getCurrentAuthenticatedUser().getId();
    }

    /**
     * Determines whether the currently authenticated user has the specified role.
     *
     * @param role the role to verify against the user's granted authorities
     * @return true if the user has the specified role; otherwise false
     */
    public static boolean hasRole(Role role) {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role.name()));
    }

    /**
     * Ensures that the currently authenticated user has the specified role.
     * <p>
     * Throws an UnauthorizedException if the required role is not present.
     * </p>
     *
     * @param role the required role
     * @throws UnauthorizedException if the user does not have the required role
     */
    public static void requireRole(Role role) {
        if (!hasRole(role)) {
            throw new UnauthorizedException("You do not have permission to perform this action");
        }
    }

    /**
     * Returns the AuthenticatedUser from the current SecurityContext.
     * <p>
     * This method verifies that the current user is authenticated and that
     * the security principal is an instance of AuthenticatedUser. Otherwise,
     * an UnauthorizedException is thrown.
     * </p>
     *
     * @return the currently authenticated AuthenticatedUser
     * @throws UnauthorizedException if authentication is missing or invalid
     */
    public static AuthenticatedUser getCurrentAuthenticatedUser() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("Authentication is required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser;
        }

        throw new UnauthorizedException("Authentication is required");
    }
}
