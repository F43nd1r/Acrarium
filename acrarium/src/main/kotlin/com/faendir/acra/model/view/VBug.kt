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
package com.faendir.acra.model.view

import com.faendir.acra.model.Bug
import com.querydsl.core.annotations.QueryProjection
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime

/**
 * @author lukas
 * @since 30.05.18
 */
class VBug @QueryProjection constructor(val bug: Bug, val lastReport: LocalDateTime, val reportCount: Long, val highestVersionCode: Int, val userCount: Long)