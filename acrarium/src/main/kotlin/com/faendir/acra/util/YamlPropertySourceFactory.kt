package com.faendir.acra.util

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.env.PropertySource.StubPropertySource
import org.springframework.core.io.support.EncodedResource
import org.springframework.core.io.support.PropertySourceFactory


class YamlPropertySourceFactory : PropertySourceFactory {
    override fun createPropertySource(name: String?, resource: EncodedResource): PropertySource<*> {
        return if (resource.resource.isReadable) {
            val factory = YamlPropertiesFactoryBean()
            factory.setResources(resource.resource)
            PropertiesPropertySource(resource.resource.filename!!, factory.getObject()!!)
        } else {
            StubPropertySource(name ?: "")
        }
    }
}