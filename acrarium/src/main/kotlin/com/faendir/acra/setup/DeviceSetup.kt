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
        processor.beans.filter { it.marketingName != null }.forEach { deviceRepository.store(it.device, it.model, it.marketingName!!) }
    }
}

@NoArgConstructor
class Device(
    @Parsed(field = ["Device"])
    val device: String,
    @Parsed(field = ["Model"])
    val model: String,
    @Parsed(field = ["Marketing Name"])
    val marketingName: String?
)