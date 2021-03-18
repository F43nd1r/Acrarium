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
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

/**
 * @author lukas
 * @since 26.07.18
 */
@Entity
class Version(@OnDelete(action = OnDeleteAction.CASCADE)
              @ManyToOne(cascade = [CascadeType.MERGE, CascadeType.REFRESH], optional = false, fetch = FetchType.LAZY)
              val app: App,
              val code: Int,
              var name: String,
              @Basic(fetch = FetchType.LAZY)
              @Type(type = "text")
              var mappings: String? = null) : Comparable<Version> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id = 0

    override fun compareTo(other: Version): Int = code.compareTo(other.code)

    override fun equals(other: Any?) = equalsBy(other, Version::id)

    override fun hashCode(): Int = Objects.hash(id)
}