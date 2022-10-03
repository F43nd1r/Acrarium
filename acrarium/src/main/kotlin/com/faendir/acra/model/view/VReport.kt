package com.faendir.acra.model.view

import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

class VReport @QueryProjection constructor(
    val id: String,
    val date: LocalDateTime,
    val androidVersion: String,
    val phoneModel: String,
    val installationId: String,
    val isSilent: Boolean,
    val marketingName: String?,
    val customColumns: List<String>,
    val stacktrace: String,
    val versionName: String,
    val bugId: Int,
    val appId: Int,
)