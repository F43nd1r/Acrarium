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

import com.faendir.acra.util.NoArgConstructor
import java.io.Serializable
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.ManyToOne

/**
 * @author lukas
 * @since 28.07.18
 */
@Entity
@IdClass(StacktraceMatch.ID::class)
class StacktraceMatch(@ManyToOne @Id val left: Stacktrace, @ManyToOne @Id val right: Stacktrace, val score: Int) {

    val both: List<Stacktrace>
        get() = listOf(left, right)

    @NoArgConstructor
    internal data class ID(private val left: Stacktrace, private val right: Stacktrace) : Serializable
}