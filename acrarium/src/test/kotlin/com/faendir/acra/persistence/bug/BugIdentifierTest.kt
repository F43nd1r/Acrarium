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
package com.faendir.acra.persistence.bug

import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.settings.AcrariumConfiguration
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class BugIdentifierTest {
    private val config = mockk<AcrariumConfiguration>()
    private val appId = AppId(1)

    @BeforeEach
    fun setup() {
        every { config.messageIgnoreRegex } returns Regex("")
    }

    @Test
    fun `fromStacktrace should trim leading whitespace from exceptionClass`() {
        val stacktrace = " com.example.MyException: some message\n\tat com.example.Foo.bar(Foo.java:42)"

        val identifier = BugIdentifier.fromStacktrace(config, appId, stacktrace)

        expectThat(identifier.exceptionClass).isEqualTo("com.example.MyException")
    }

    @Test
    fun `fromStacktrace should extract crashLine from stacktrace`() {
        val stacktrace = "com.example.MyException: message\n" +
                "\tat android.os.Handler.dispatchMessage(Handler.java:99)\n" +
                "\tat com.example.Foo.bar(Foo.java:42)"

        val identifier = BugIdentifier.fromStacktrace(config, appId, stacktrace)

        // android.* lines are skipped; first non-android/java line should be captured
        expectThat(identifier.crashLine).isEqualTo("com.example.Foo.bar(Foo.java:42)")
    }

    @Test
    fun `fromStacktrace should extract cause from stacktrace`() {
        val stacktrace = "com.example.MyException: message\n" +
                "\tat com.example.Foo.bar(Foo.java:42)\n" +
                "Caused by: com.example.RootCause: root message\n" +
                "\tat com.example.Foo.baz(Foo.java:10)"

        val identifier = BugIdentifier.fromStacktrace(config, appId, stacktrace)

        expectThat(identifier.cause).isEqualTo("com.example.RootCause: root message")
    }

    @Test
    fun `fromStacktrace should trim whitespace from cause`() {
        val stacktrace = "com.example.MyException: msg\n" +
                "\tat com.example.Foo.bar(Foo.java:42)\n" +
                "Caused by: com.example.RootCause: root msg  \n" +
                "\tat com.example.Foo.baz(Foo.java:10)"

        val identifier = BugIdentifier.fromStacktrace(config, appId, stacktrace)

        expectThat(identifier.cause).isEqualTo("com.example.RootCause: root msg")
    }

    @Test
    fun `fromStacktrace should trim whitespace from message`() {
        val stacktrace = "com.example.MyException:  message with leading space\n\tat com.example.Foo.bar(Foo.java:42)"

        val identifier = BugIdentifier.fromStacktrace(config, appId, stacktrace)

        expectThat(identifier.message).isEqualTo("message with leading space")
    }

}
