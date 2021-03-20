/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
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

package com.faendir.acra.liquibase.changelog

object ColumnType {
    const val STRING = "VARCHAR(255)"
    const val INT = "INT"
    const val BOOLEAN = "BOOLEAN"
    const val BLOB = "LONGBLOB"
    const val TEXT = "LONGTEXT"
    const val DATETIME = "DATETIME"
}

object Author {
    const val F43ND1R = "f43nd1r"
}

object Table {
    const val STACKTRACE = "stacktrace"
}