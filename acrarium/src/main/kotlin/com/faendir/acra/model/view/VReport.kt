package com.faendir.acra.model.view

import com.faendir.acra.model.Stacktrace
import com.querydsl.core.annotations.QueryProjection
import java.time.ZonedDateTime

class VReport @QueryProjection constructor(
    val stacktrace: Stacktrace,
    val id: String,
    val date: ZonedDateTime,
    val androidVersion: String,
    val phoneModel: String,
    val installationId: String,
    val isSilent: Boolean,
    val marketingName: String?,
    val customColumns: List<String>
)