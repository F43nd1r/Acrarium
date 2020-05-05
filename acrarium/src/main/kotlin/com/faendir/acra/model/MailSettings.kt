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
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.io.Serializable
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

/**
 * @author lukas
 * @since 07.12.18
 */
@Entity
@IdClass(MailSettings.ID::class)
class MailSettings(@OnDelete(action = OnDeleteAction.CASCADE)
                   @ManyToOne
                   @Id
                   val app: App, @JoinColumn(name = "username")
                   @OnDelete(action = OnDeleteAction.CASCADE)
                   @ManyToOne
                   @Id
                   val user: User) {

    var newBug = false
    var regression = false
    var spike = false
    var summary = false

    @NoArgConstructor
    internal data class ID(val app: Int, val user: String) : Serializable

}