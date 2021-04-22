package com.faendir.acra.setup

import com.faendir.acra.model.Device
import com.faendir.acra.service.DataService
import com.faendir.acra.settings.AcrariumConfiguration
import com.univocity.parsers.common.processor.BeanListProcessor
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils
import java.io.InputStream
import java.net.URL

@Profile("!test")
@Component
class DeviceSetup(private val dataService: DataService, private val configuration: AcrariumConfiguration) {

    @EventListener
    fun onStartup(event: ContextRefreshedEvent) {
        if(configuration.updateDeviceList) {
            val url = URL("https://storage.googleapis.com/play_public/supported_devices.csv")
            fillDeviceTable(url.openStream())
        } else if(!dataService.hasDeviceTableEntries()) {
            fillDeviceTable(ClassPathResource("devices.csv").inputStream)
        }
    }

    private fun fillDeviceTable(stream: InputStream) {
        val processor = BeanListProcessor(Device::class.java)
        CsvParser(CsvParserSettings().apply {
            isHeaderExtractionEnabled = true
            setProcessor(processor)
        }).parse(stream.bufferedReader(Charsets.UTF_16LE))
        dataService.updateDeviceTable(processor.beans.filter { it.marketingName != null })
    }
}