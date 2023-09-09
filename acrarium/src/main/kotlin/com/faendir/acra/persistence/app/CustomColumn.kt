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
package com.faendir.acra.persistence.app

import com.faendir.acra.jooq.generated.tables.references.REPORT
import org.jooq.Field
import org.jooq.Index
import org.jooq.impl.DSL


data class CustomColumn(val name: String, val path: String) {
    val fieldName = "custom_$path"
    val field = DSL.field(DSL.name(REPORT.name, fieldName))
    val indexName = "idx_custom_$path"
}

val Field<*>.isCustomColumn get() = name.startsWith("custom_")
val Index.isCustomColumnIndex get() = name.startsWith("idx_custom_")