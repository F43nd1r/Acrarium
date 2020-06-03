package com.faendir.acra.selenium

import com.faendir.acra.ui.component.UserEditor
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.platform.commons.util.ReflectionUtils
import org.openqa.selenium.By
import org.openqa.selenium.Capabilities
import org.openqa.selenium.OutputType
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.testcontainers.containers.BrowserWebDriverContainer
import java.io.IOException
import java.lang.annotation.Inherited
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import java.util.stream.Stream


class KBrowserWebDriverContainer : BrowserWebDriverContainer<KBrowserWebDriverContainer>()

class BrowserArgumentsProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
        return Stream.of(Arguments.of(createContainer(ChromeOptions()), "Chrome"),
                Arguments.of(createContainer(FirefoxOptions()), "Firefox"))
    }

    private fun createContainer(options: Capabilities): KBrowserWebDriverContainer {
        return KBrowserWebDriverContainer().withCapabilities(options)
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@ParameterizedTest(name = "[{index}] {1}")
@ArgumentsSource(BrowserArgumentsProvider::class)
annotation class SeleniumTest

class ContainerExtension : BeforeTestExecutionCallback, AfterTestExecutionCallback {
    override fun beforeTestExecution(context: ExtensionContext) {
        argumentsFrom(context).filterIsInstance<KBrowserWebDriverContainer>().forEach {
            it.start()
            it.webDriver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
        }
    }

    override fun afterTestExecution(context: ExtensionContext) {
        argumentsFrom(context).filterIsInstance<KBrowserWebDriverContainer>().forEach {
            if (context.executionException.isPresent) {
                val screenshot = it.webDriver.getScreenshotAs(OutputType.BYTES)
                try {
                    val path: Path = Paths
                            .get("target/selenium-screenshots")
                            .resolve(String.format("%s-%s-%s.png",
                                    LocalDateTime.now(),
                                    context.requiredTestClass.name,
                                    context.requiredTestMethod.name))
                    Files.createDirectories(path.parent)
                    Files.write(path, screenshot)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            it.stop()
        }
    }

    private fun argumentsFrom(context: ExtensionContext): Array<Any?> {
        return try {
            val method: Method = ReflectionUtils.findMethod(context.javaClass, "getTestDescriptor").orElse(null)
            val descriptor = ReflectionUtils.invokeMethod(method, context) as TestMethodTestDescriptor

            //Get the TestTemplateInvocationContext
            val templateField: Field = descriptor.javaClass.getDeclaredField("invocationContext")
            templateField.isAccessible = true
            val template = templateField.get(descriptor) as TestTemplateInvocationContext

            //Get the params finally
            val argumentsField: Field = template.javaClass.getDeclaredField("arguments")
            argumentsField.isAccessible = true
            argumentsField.get(template) as Array<Any?>
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
    }
}

fun RemoteWebDriver.findInputById(id: String) = findElementById(id).findElement(By.tagName("input"))