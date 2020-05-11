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

import com.faendir.acra.util.equalsBy
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.Type
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

/**
 * @author Lukas
 * @since 08.12.2017
 */
@Entity
class Bug(@OnDelete(action = OnDeleteAction.CASCADE)
          @ManyToOne(cascade = [CascadeType.MERGE, CascadeType.REFRESH], optional = false, fetch = FetchType.LAZY) @JsonIdentityReference(alwaysAsId = true)
          @JsonIdentityInfo(generator = PropertyGenerator::class, property = "id")
          val app: App,
          stacktrace: String) {
    @Type(type = "text")
    var title: String = stacktrace.split("\n".toRegex(), 2).toTypedArray()[0]

    @ManyToOne(cascade = [CascadeType.MERGE, CascadeType.REFRESH], fetch = FetchType.EAGER)
    @JoinColumn(name = "solved_version")
    var solvedVersion: Version? = null

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id = 0

    override fun equals(other: Any?) = equalsBy(other, Bug::id)

    override fun hashCode(): Int = id

}