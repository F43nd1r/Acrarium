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
package com.faendir.acra.model.view

import com.faendir.acra.model.Permission
import com.faendir.acra.model.QApp
import com.faendir.acra.model.QPermission
import com.faendir.acra.model.QUser
import com.faendir.acra.model.User
import com.faendir.acra.security.SecurityUtils.getUsername
import com.faendir.acra.security.SecurityUtils.hasRole
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.JPAExpressions

/**
 * @author lukas
 * @since 24.08.18
 */
object WhereExpressions {

    @JvmStatic
    fun whereHasAppPermission(): BooleanExpression {
        val base = JPAExpressions.select(QPermission.permission).from(QUser.user).join(QUser.user.permissions, QPermission.permission)
        return if (hasRole(User.Role.ADMIN)) {
            base.where(QUser.user.username.eq(getUsername()).and(QPermission.permission.app.eq(QApp.app)).and(QPermission.permission.level.lt(Permission.Level.VIEW))).notExists()
        } else {
            base.where(QUser.user.username.eq(getUsername()).and(QPermission.permission.app.eq(QApp.app)).and(QPermission.permission.level.goe(Permission.Level.VIEW))).exists()
        }
    }
}