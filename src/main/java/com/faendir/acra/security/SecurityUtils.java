package com.faendir.acra.security;

import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.sql.user.UserManager;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    public static boolean hasRole(@NonNull String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority(role));
    }

    @NonNull
    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "";
    }

    public static boolean hasPermission(App app, @NonNull Permission.Level level) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream().filter(authority -> authority instanceof Permission).map(Permission.class::cast)
                .filter(permission -> permission.getApp().equals(app)).findAny().map(Permission::getLevel)
                .orElseGet(() -> hasRole(UserManager.ROLE_ADMIN) ? Permission.Level.ADMIN : Permission.Level.NONE).ordinal() >= level.ordinal();
    }
}
