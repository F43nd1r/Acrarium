package com.faendir.acra.setup

import com.faendir.acra.model.Device
import com.faendir.acra.service.DataService
import com.univocity.parsers.common.processor.BeanListProcessor
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.net.URL

@Profile("!test")
@Component
class DeviceSetup(private val dataService: DataService) {

    @EventListener
    fun onStartup(event: ContextRefreshedEvent) {
        val processor = BeanListProcessor(Device::class.java)
        CsvParser(CsvParserSettings().apply {
            isHeaderExtractionEnabled = true
            setProcessor(processor)
        }).parse(URL("https://storage.googleapis.com/play_public/supported_devices.csv").openStream().bufferedReader(Charsets.UTF_16LE))
        dataService.updateDeviceTable(processor.beans.filter { it.marketingName != null })
    }
}