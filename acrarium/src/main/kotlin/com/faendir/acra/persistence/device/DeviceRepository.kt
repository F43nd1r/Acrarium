package com.faendir.acra.persistence.device

import com.faendir.acra.jooq.generated.Tables.DEVICE
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