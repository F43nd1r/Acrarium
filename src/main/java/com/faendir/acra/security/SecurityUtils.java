package com.faendir.acra.security;

import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.User;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    public static boolean hasRole(@NonNull User.Role role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().contains(role);
    }

    @NonNull
    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "";
    }

    public static boolean hasPermission(@NonNull App app, @NonNull Permission.Level level) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
               && getPermission(app, authentication.getAuthorities().stream().filter(Permission.class::isInstance).map(Permission.class::cast),
                () -> hasRole(User.Role.ADMIN)).ordinal() >= level.ordinal();
    }

    public static Permission.Level getPermission(@NonNull App app, @NonNull User user) {
        return getPermission(app, user.getPermissions().stream(), () -> user.getRoles().contains(User.Role.ADMIN));
    }

    @NonNull
    private static Permission.Level getPermission(@NonNull App app, @NonNull Stream<Permission> permissionStream, BooleanSupplier isAdmin) {
        return permissionStream.filter(permission -> permission.getApp().equals(app))
                .findAny()
                .map(Permission::getLevel)
                .orElseGet(() -> isAdmin.getAsBoolean() ? Permission.Level.ADMIN : Permission.Level.NONE);
    }
}
