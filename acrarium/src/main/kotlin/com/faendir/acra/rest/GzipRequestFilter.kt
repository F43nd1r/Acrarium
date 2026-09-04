/*
 * (C) Copyright 2026 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.rest

import jakarta.servlet.FilterChain
import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.io.input.BoundedInputStream
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.util.unit.DataSize
import org.springframework.beans.factory.annotation.Value
import java.io.IOException
import java.io.InputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.GZIPInputStream

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class GzipRequestFilter(@Value("\${spring.servlet.multipart.max-request-size}") private val maxRequestSize: DataSize) : HttpFilter() {
    override fun doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        if (request.servletPath == "/${RestReportInterface.REPORT_PATH}" && request.getHeader(HttpHeaders.CONTENT_ENCODING)?.equals(GZIP, ignoreCase = true) == true) {
            chain.doFilter(GzipRequestWrapper(request, maxRequestSize.toBytes()), response)
        } else {
            chain.doFilter(request, response)
        }
    }

    private companion object {
        const val GZIP = "gzip"
    }

    private class GzipRequestWrapper(request: HttpServletRequest, maxRequestSize: Long) : HttpServletRequestWrapper(request) {
        private val inputStream = ServletInputStreamAdapter(
            BoundedInputStream.builder()
                .setInputStream(
                    GZIPInputStream(
                        BoundedInputStream.builder()
                            .setInputStream(request.inputStream)
                            .setMaxCount(maxRequestSize)
                            .setOnMaxCount { _, _ -> throw IOException("Compressed request body exceeds $maxRequestSize bytes") }
                            .get()
                    )
                )
                .setMaxCount(maxRequestSize)
                .setOnMaxCount { _, _ -> throw IOException("Decompressed request body exceeds $maxRequestSize bytes") }
                .get()
        )

        override fun getInputStream(): ServletInputStream = inputStream

        override fun getReader(): BufferedReader = BufferedReader(InputStreamReader(inputStream, characterEncoding?.let(Charset::forName) ?: StandardCharsets.UTF_8))

        override fun getContentLength(): Int = -1

        override fun getContentLengthLong(): Long = -1

        override fun getHeader(name: String): String? =
            if (name.equals(HttpHeaders.CONTENT_ENCODING, ignoreCase = true) || name.equals(HttpHeaders.CONTENT_LENGTH, ignoreCase = true)) null else super.getHeader(name)

        override fun getHeaders(name: String): Enumeration<String?>? =
            if (name.equals(HttpHeaders.CONTENT_ENCODING, ignoreCase = true) || name.equals(
                    HttpHeaders.CONTENT_LENGTH,
                    ignoreCase = true
                )
            ) Collections.emptyEnumeration() else super.getHeaders(name)
    }

    private class ServletInputStreamAdapter(private val delegate: InputStream) : ServletInputStream() {
        private var finished = false

        override fun read(): Int = delegate.read().also { finished = it == -1 }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int = delegate.read(buffer, offset, length).also { finished = it == -1 }

        override fun isFinished(): Boolean = finished

        override fun isReady(): Boolean = true

        override fun setReadListener(readListener: ReadListener) {
            throw UnsupportedOperationException("Asynchronous reads are not supported")
        }
    }
}
