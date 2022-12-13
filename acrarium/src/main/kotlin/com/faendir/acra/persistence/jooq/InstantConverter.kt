package com.faendir.acra.persistence.jooq

import com.faendir.acra.util.toUtcLocal
import org.jooq.impl.AbstractConverter
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Component
class InstantConverter : AbstractConverter<LocalDateTime, Instant>(LocalDateTime::class.java, Instant::class.java) {
    override fun from(databaseObject: LocalDateTime?): Instant? = databaseObject?.toInstant(ZoneOffset.UTC)

    override fun to(userObject: Instant?): LocalDateTime? = userObject?.toUtcLocal()

}