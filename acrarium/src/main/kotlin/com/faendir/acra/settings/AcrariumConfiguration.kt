package com.faendir.acra.settings

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration

@ConstructorBinding
@ConfigurationProperties("acrarium")
class AcrariumConfiguration(val updateDeviceList: Boolean)