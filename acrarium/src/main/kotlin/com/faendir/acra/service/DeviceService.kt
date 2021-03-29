package com.faendir.acra.service

import com.faendir.acra.model.Device
import com.univocity.parsers.common.Context
import com.univocity.parsers.common.processor.BeanListProcessor
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.net.URL

@Service
class DeviceService(private val dataService: DataService) {

    @EventListener
    fun onStartup(event: ContextRefreshedEvent) {
        val processor = BeanListProcessor(Device::class.java)
        CsvParser(CsvParserSettings().apply {
            isHeaderExtractionEnabled = true
            setProcessor(processor)
        }).parse(URL("https://storage.googleapis.com/play_public/supported_devices.csv").openStream())
        dataService.updateDeviceTable(processor.beans.filter { it.marketingName != null })
    }
}