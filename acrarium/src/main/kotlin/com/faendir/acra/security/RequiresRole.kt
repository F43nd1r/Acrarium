package com.faendir.acra.security

import com.faendir.acra.model.User

annotation class RequiresRole(val value: User.Role)
