package com.mediconnect.util;

import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.Role;
import com.mediconnect.security.AuthenticatedUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for security-related operations.
 * <p>
 * Provides static methods to access the current authentication context,
 * retrieve the currently authenticated user's email and ID, check roles,
 * and enforce role-based permissions. This class is intended to be used
 * throughout the application for security checks and user context retrieval.
 * </p>
 */
public final class SecurityUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private SecurityUtils() {
    }

    /**
     * Retrieves the current Authentication object from the SecurityContext.
     *
     * @return the Authentication object, or null if none is set
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Gets the email of the currently authenticated user.
     *
     * @return the email of the authenticated user
     * @throws UnauthorizedException if no authenticated user is found
     */
    public static String getCurrentUserEmail() {
        return getCurrentAuthenticatedUser().getEmail();
    }

    /**
     * Gets the ID of the currently authenticated user.
     *
     * @return the ID of the authenticated user
     * @throws UnauthorizedException if no authenticated user is found
     */
    public static Long getCurrentUserId() {
        return getCurrentAuthenticatedUser().getId();
    }

    /**
     * Checks whether the currently authenticated user has the specified role.
     *
     * @param role the role to check against the user's granted authorities
     * @return true if the user has the role, false otherwise (including if not authenticated)
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
     * Enforces that the currently authenticated user must have the specified role.
     * <p>
     * If the user does not have the role, an UnauthorizedException is thrown.
     * </p>
     *
     * @param role the required role
     * @throws UnauthorizedException if the user does not have the role
     */
    public static void requireRole(Role role) {
        if (!hasRole(role)) {
            throw new UnauthorizedException("You do not have permission to perform this action");
        }
    }

    /**
     * Retrieves the AuthenticatedUser object from the current SecurityContext.
     * <p>
     * This method validates that the user is authenticated and that the principal
     * is an instance of AuthenticatedUser. If not, an UnauthorizedException is thrown.
     * </p>
     *
     * @return the AuthenticatedUser representing the currently logged-in user
     * @throws UnauthorizedException if not authenticated or principal is invalid
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