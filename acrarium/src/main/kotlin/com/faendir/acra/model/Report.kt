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

import com.faendir.acra.util.toDate
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator
import com.querydsl.core.annotations.QueryInit
import org.acra.ReportField
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.Type
import org.json.JSONObject
import java.time.ZonedDateTime
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Transient

/**
 * @author Lukas
 * @since 08.12.2017
 */
@Entity
class Report(@OnDelete(action = OnDeleteAction.CASCADE)
             @ManyToOne(cascade = [CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH], optional = false, fetch = FetchType.EAGER)
             @JsonIdentityReference(alwaysAsId = true)
             @JsonIdentityInfo(generator = PropertyGenerator::class, property = "id")
             @QueryInit("*.*")
             var stacktrace: Stacktrace,
             @Type(type = "text")
             val content: String) {

    @JsonIgnore
    @Transient
    private lateinit var jsonObjectField: JSONObject

    val jsonObject: JSONObject
        get() {
            if (!::jsonObjectField.isInitialized) {
                jsonObjectField = JSONObject(content)
            }
            return jsonObjectField
        }


    @Id
    val id: String = jsonObject.getString(ReportField.REPORT_ID.name)

    val date: ZonedDateTime = jsonObject.optString(ReportField.USER_CRASH_DATE.name).toDate()

    val userEmail: String = jsonObject.optString(ReportField.USER_EMAIL.name)

    @Type(type = "text")
    val userComment: String = jsonObject.optString(ReportField.USER_COMMENT.name)

    val androidVersion: String = jsonObject.optString(ReportField.ANDROID_VERSION.name)

    val phoneModel: String = jsonObject.optString(ReportField.PHONE_MODEL.name)

    val brand: String = jsonObject.optString(ReportField.BRAND.name)

    val installationId: String = jsonObject.optString(ReportField.INSTALLATION_ID.name)

    @Column(name = "is_silent")
    val isSilent: Boolean = jsonObject.optBoolean(ReportField.IS_SILENT.name)
}