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
package com.faendir.acra.domain

import com.faendir.acra.annotation.AcrariumTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isNotSameInstanceAs
import strikt.assertions.isSameInstanceAs

@AcrariumTest
class AvatarServiceTest(
    @Autowired private val avatarService: AvatarService,
) {
    @Test
    fun `getAvatarResource should cache result for same installation id`() {
        val first = avatarService.getAvatarResource("cache-test-same-id")
        val second = avatarService.getAvatarResource("cache-test-same-id")
        expectThat(first).isSameInstanceAs(second)
    }

    @Test
    fun `getAvatarResource should return distinct handlers for different installation ids`() {
        val first = avatarService.getAvatarResource("cache-test-id-1")
        val second = avatarService.getAvatarResource("cache-test-id-2")
        expectThat(first).isNotSameInstanceAs(second)
    }
}
