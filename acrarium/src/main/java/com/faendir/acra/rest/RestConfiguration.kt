/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.rest

import com.github.ziplet.filter.compression.CompressingFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import javax.servlet.Filter

/**
 * @author lukas
 * @since 16.05.18
 */
@Configuration
open class RestConfiguration {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    open fun gzipFilter(): Filter = CompressingFilter()
}