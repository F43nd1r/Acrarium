package com.faendir.acra.settings

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import java.util.regex.Pattern


@ConfigurationProperties("acrarium")
class AcrariumConfiguration @ConstructorBinding constructor(val updateDeviceList: Boolean, private val messageIgnorePattern: Pattern) {
    val messageIgnoreRegex get() = messageIgnorePattern.toRegex()
}