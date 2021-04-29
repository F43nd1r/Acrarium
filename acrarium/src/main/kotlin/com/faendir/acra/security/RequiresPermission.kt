package com.faendir.acra.security

import com.faendir.acra.model.Permission

annotation class RequiresPermission(val value: Permission.Level)
