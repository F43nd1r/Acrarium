/*
 * (C) Copyright 2022 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.persistence.device

import com.faendir.acra.jooq.generated.tables.references.DEVICE
import com.faendir.acra.persistence.fetchValue
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class DeviceRepository(private val jooq: DSLContext) {
    fun findMarketingName(phoneModel: String, device: String): String? = jooq
        .select(DEVICE.MARKETING_NAME)
        .from(DEVICE).where(
            DEVICE.DEVICE_.eq(device).and(DEVICE.MODEL.eq(phoneModel))
        ).fetchValue()

    fun isEmpty() = jooq.selectOne().from(DEVICE).limit(1).fetchValue() == null

    @Transactional
    fun store(device: String, model: String, marketingDevice: String) =
        jooq.insertInto(DEVICE)
            .set(DEVICE.DEVICE_, device)
            .set(DEVICE.MODEL, model)
            .set(DEVICE.MARKETING_NAME, marketingDevice)
            .onDuplicateKeyUpdate()
            .set(DEVICE.MARKETING_NAME, marketingDevice)
            .execute()
}