package com.faendir.acra.security

import com.faendir.acra.persistence.user.Permission

annotation class RequiresPermission(val value: Permission.Level)
