package com.faendir.acra.ui.testbench

import com.faendir.acra.util.toNullable
import com.vaadin.testbench.TestBenchTestCase
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor
import org.springframework.util.ReflectionUtils.findField
import org.springframework.util.ReflectionUtils.findMethod
import org.springframework.util.ReflectionUtils.getField
import org.springframework.util.ReflectionUtils.invokeMethod
import org.springframework.util.ReflectionUtils.makeAccessible

class ContainerExtension : BeforeTestExecutionCallback,
        AfterTestExecutionCallback {
    override fun beforeTestExecution(context: ExtensionContext) {
        argumentsFrom(context).filterIsInstance<KBrowserWebDriverContainer>().forEach {
            it.start()
            (context.testInstance.toNullable() as? TestBenchTestCase)?.driver = it.webDriver
        }
    }

    override fun afterTestExecution(context: ExtensionContext) {
        argumentsFrom(context).filterIsInstance<KBrowserWebDriverContainer>().forEach { it.stop() }
    }

    private fun argumentsFrom(context: ExtensionContext): Array<Any?> {
        return try {
            context.callMethod<TestMethodTestDescriptor>("getTestDescriptor").getFieldValue<TestTemplateInvocationContext>("invocationContext").getFieldValue("arguments")
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
    }

    private inline fun <reified T> Any.callMethod(name: String): T {
        return invokeMethod(findMethod(javaClass, name)!!.also { makeAccessible(it) }, this) as T
    }

    private inline fun <reified T> Any.getFieldValue(name: String): T {
        return getField(findField(javaClass, name)!!.also { makeAccessible(it) }, this) as T
    }
}