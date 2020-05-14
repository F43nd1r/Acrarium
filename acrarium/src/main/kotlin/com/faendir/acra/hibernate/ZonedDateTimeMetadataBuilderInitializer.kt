/*
 * (C) Copyright 2020 Lukas Morawietz (https://github.com/F43nd1r)
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

package com.faendir.acra.hibernate

import com.google.auto.service.AutoService
import org.hibernate.boot.MetadataBuilder
import org.hibernate.boot.registry.StandardServiceRegistry
import org.hibernate.boot.spi.MetadataBuilderInitializer
import org.hibernate.dialect.function.SQLFunctionTemplate
import org.hibernate.type.ZonedDateTimeType

@AutoService(MetadataBuilderInitializer::class)
class ZonedDateTimeMetadataBuilderInitializer : MetadataBuilderInitializer {
    override fun contribute(metadataBuilder: MetadataBuilder, serviceRegistry: StandardServiceRegistry) {
        metadataBuilder.applySqlFunction("DATE", SQLFunctionTemplate(ZonedDateTimeType.INSTANCE, "DATE(?1)"))
    }
}