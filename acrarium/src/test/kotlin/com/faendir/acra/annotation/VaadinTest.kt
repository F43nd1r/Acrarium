package com.faendir.acra.annotation

import com.faendir.acra.ui.testbench.BrowserArgumentsProvider
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@ParameterizedTest(name = "[{index}] {1}")
@ArgumentsSource(BrowserArgumentsProvider::class)
annotation class VaadinTest