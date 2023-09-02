/*
 * (C) Copyright 2021-2023 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.setup

import com.faendir.acra.persistence.device.DeviceRepository
import com.faendir.acra.settings.AcrariumConfiguration
import com.faendir.acra.util.NoArgConstructor
import com.univocity.parsers.annotations.Parsed
import com.univocity.parsers.common.processor.BeanListProcessor
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import org.springframework.context.annotation.Profile
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.InputStream
import java.net.URL

@Profile("!test")
@Component
class DeviceSetup(private val deviceRepository: DeviceRepository, private val configuration: AcrariumConfiguration) {

    @EventListener
    fun onStartup(event: ContextRefreshedEvent) {
        if (configuration.updateDeviceList) {
            val url = URL("https://storage.googleapis.com/play_public/supported_devices.csv")
            fillDeviceTable(url.openStream())
        } else if (deviceRepository.isEmpty()) {
            fillDeviceTable(ClassPathResource("devices.csv").inputStream)
        }
    }

    private fun fillDeviceTable(stream: InputStream) {
        val processor = BeanListProcessor(Device::class.java)
        CsvParser(CsvParserSettings().apply {
            isHeaderExtractionEnabled = true
            setProcessor(processor)
        }).parse(stream.bufferedReader(Charsets.UTF_16LE))
        processor.beans.filter { it.device != null && it.marketingName != null }.forEach { deviceRepository.store(it.device!!, it.model, it.marketingName!!) }
    }
}

@NoArgConstructor
class Device(
    @Parsed(field = ["Device"])
    val device: String?,
    @Parsed(field = ["Model"])
    val model: String,
    @Parsed(field = ["Marketing Name"])
    val marketingName: String?
)