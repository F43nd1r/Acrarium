package com.faendir.acra.ui.selenium

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.openqa.selenium.Capabilities
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import java.util.stream.Stream

class BrowserArgumentsProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
        return Stream.of(
                Arguments.of(createContainer(ChromeOptions()), "Chrome"),
                Arguments.of(createContainer(FirefoxOptions()), "Firefox"))
    }

    private fun createContainer(options: Capabilities): KBrowserWebDriverContainer {
        return KBrowserWebDriverContainer().withCapabilities(options)
    }
}