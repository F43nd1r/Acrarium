/*
 * (C) Copyright 2022-2023 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.persistence.version

import com.faendir.acra.jooq.generated.tables.references.VERSION
import com.faendir.acra.persistence.SortDefinition
import com.faendir.acra.persistence.app.AppId
import org.jooq.Field

data class Version(
    val code: Int,
    val name: String,
    @get:JvmName("getAppId")
    val appId: AppId,
    val mappings: String? = null,
    val flavor: String,
) {
    enum class Sort(override val field: Field<*>) : SortDefinition {
        CODE(VERSION.CODE),
        NAME(VERSION.NAME),
        MAPPINGS(VERSION.MAPPINGS)
    }
}

data class VersionKey(
    val code: Int,
    val flavor: String,
)

data class VersionName(
    val code: Int,
    val flavor: String,
    val name: String
)

fun Version.toVersionKey() = VersionKey(code, flavor)
fun VersionName.toVersionKey() = VersionKey(code, flavor)
