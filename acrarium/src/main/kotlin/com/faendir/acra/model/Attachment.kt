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
import org.springframework.data.annotation.PersistenceConstructor
import org.springframework.lang.NonNull
import java.io.Serializable
import java.sql.Blob
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.Lob
import javax.persistence.ManyToOne

/**
 * @author Lukas
 * @since 11.12.2017
 */
@Entity
@IdClass(Attachment.MetaData::class)
class Attachment(@ManyToOne(cascade = [CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH], optional = false)
                 @Id
                 private val report: Report,
                 @Id
                 val filename: String,
                 @Lob
                 val content: Blob) {

    @NoArgConstructor
    data class MetaData(private val report : Report, private val filename : String) : Serializable
}