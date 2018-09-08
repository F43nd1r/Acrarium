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
package com.faendir.acra.model.view;

import com.faendir.acra.model.Permission;
import com.faendir.acra.model.User;
import com.faendir.acra.security.SecurityUtils;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;

import static com.faendir.acra.model.QApp.app;
import static com.faendir.acra.model.QPermission.permission;
import static com.faendir.acra.model.QUser.user;

/**
 * @author lukas
 * @since 24.08.18
 */
public class WhereExpressions {
    private static final BooleanExpression APP_PERMISSION_ADMIN = JPAExpressions.select(permission)
            .from(user)
            .join(user.permissions, permission)
            .where(user.username.eq(SecurityUtils.getUsername()).and(permission.app.eq(app)).and(permission.level.lt(Permission.Level.VIEW)))
            .notExists();

    private static final BooleanExpression APP_PERMISSION_NO_ADMIN = JPAExpressions.select(permission)
            .from(user)
            .join(user.permissions, permission)
            .where(user.username.eq(SecurityUtils.getUsername()).and(permission.app.eq(app)).and(permission.level.goe(Permission.Level.VIEW)))
            .exists();

    public static BooleanExpression whereHasAppPermission() {
        return SecurityUtils.hasRole(User.Role.ADMIN) ? WhereExpressions.APP_PERMISSION_ADMIN : WhereExpressions.APP_PERMISSION_NO_ADMIN;
    }
}
