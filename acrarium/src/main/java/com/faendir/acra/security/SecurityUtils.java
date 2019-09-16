/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
