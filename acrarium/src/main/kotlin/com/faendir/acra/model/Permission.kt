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
package com.faendir.acra.model

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.security.core.GrantedAuthority
import javax.persistence.Embeddable
import javax.persistence.ManyToOne

/**
 * @author Lukas
 * @since 20.05.2017
 */
@Embeddable
class Permission(@OnDelete(action = OnDeleteAction.CASCADE)
                 @ManyToOne
                 val app: App,
                 var level: Level) : GrantedAuthority {

    override fun getAuthority(): String {
        return "PERMISSION_" + level.name + "_" + app
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null || javaClass != other.javaClass -> false
            else -> app == (other as Permission).app
        }
    }

    override fun hashCode(): Int = app.hashCode()

    enum class Level {
        NONE, VIEW, EDIT, ADMIN
    }
}