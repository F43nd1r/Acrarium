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
package com.faendir.acra.settings

import com.faendir.acra.util.YamlPropertySourceFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource(value = [$$"file:${user.home}/.config/acrarium/application.properties"], ignoreResourceNotFound = true)
@PropertySource(value = [$$"file:${user.home}/.config/acrarium/application.yml"], ignoreResourceNotFound = true, factory = YamlPropertySourceFactory::class)
@PropertySource(value = [$$"file:${user.home}/.acra/application.properties"], ignoreResourceNotFound = true)
@PropertySource(value = [$$"file:${user.home}/.acra/application.yml"], ignoreResourceNotFound = true, factory = YamlPropertySourceFactory::class)
class ExternalPropertySourceConfiguration