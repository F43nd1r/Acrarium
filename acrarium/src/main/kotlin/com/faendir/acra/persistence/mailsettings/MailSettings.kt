package com.faendir.acra.persistence.mailsettings

import com.faendir.acra.persistence.app.AppId

data class MailSettings(
    val appId: AppId,
    val username: String,
    var newBug: Boolean = false,
    var regression: Boolean = false,
    var spike: Boolean = false,
    var summary: Boolean = false
)