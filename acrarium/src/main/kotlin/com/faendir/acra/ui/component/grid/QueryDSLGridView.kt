package com.faendir.acra.ui.component.grid

import com.faendir.acra.dataprovider.QueryDslDataProvider
import com.faendir.acra.settings.GridSettings
import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import kotlin.reflect.KMutableProperty0

class QueryDSLGridView<T : Any>(dataProvider: QueryDslDataProvider<T>, gridSettings: KMutableProperty0<GridSettings?>, initializer: QueryDslAcrariumGrid<T>.() -> Unit = {})
    : AcrariumGridView<T, BooleanExpression, Expression<out Comparable<*>>, QueryDslAcrariumColumn<T>, QueryDslAcrariumGrid<T>>(QueryDslAcrariumGrid(dataProvider, gridSettings.get()), gridSettings, initializer)