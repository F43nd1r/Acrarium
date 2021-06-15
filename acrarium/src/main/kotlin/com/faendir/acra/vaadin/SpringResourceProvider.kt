package com.faendir.acra.vaadin

import com.vaadin.flow.di.ResourceProvider
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

@Component
class SpringResourceProvider(private val resourceLoader: ResourceLoader) : ResourceProvider {
    private val cache: ConcurrentHashMap<String, CachedStreamData> = ConcurrentHashMap()

    override fun getApplicationResource(path: String): URL {
        val realPath = if (path.contains("appreciated")) {
            "./META-INF/resources/frontend${path.removePrefix(".")}"
        } else {
            path
        }
        return resourceLoader.getResource("classpath:$realPath").url
    }

    @Throws(IOException::class)
    override fun getApplicationResources(path: String): List<URL> {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("classpath:$path").map { it.url }
    }

    override fun getClientResource(path: String): URL {
        return getApplicationResource(path)
    }

    @Throws(IOException::class)
    override fun getClientResourceAsStream(path: String): InputStream {
        // the client resource should be available in the classpath, so
        // its content is cached once. If an exception is thrown then
        // something is broken and it's also cached and will be rethrown on
        // every subsequent access
        val cached: CachedStreamData = cache.computeIfAbsent(path) { key: String ->
            val url = getClientResource(key)
            try {
                url.openStream().use { stream -> CachedStreamData(stream.readBytes(), null) }
            } catch (e: IOException) {
                CachedStreamData(null, e)
            }
        }
        cached.exception?.let { throw it }
        return ByteArrayInputStream(cached.data)
    }

    private class CachedStreamData(val data: ByteArray?, val exception: IOException?)
}