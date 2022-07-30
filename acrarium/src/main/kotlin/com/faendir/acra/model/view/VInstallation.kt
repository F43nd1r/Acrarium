package com.faendir.acra.model.view

import com.querydsl.core.annotations.QueryProjection
import java.time.ZonedDateTime

class VInstallation @QueryProjection constructor(val id: String, val reportCount: Long, val lastReport: ZonedDateTime)