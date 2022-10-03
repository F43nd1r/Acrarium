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

package com.faendir.acra.dataprovider

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.core.types.dsl.StringPath
import com.querydsl.jpa.impl.JPAQuery
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.QuerySortOrder
import com.vaadin.flow.data.provider.SortDirection
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class QueryDslDataProviderTest {

    lateinit var jpaQuery: JPAQuery<TestDTO>
    private lateinit var dataProvider: QueryDslDataProvider<TestDTO>

    @BeforeEach
    fun setUp() {
        jpaQuery = mockk(relaxed = true)
        dataProvider = QueryDslDataProvider({ jpaQuery }, { jpaQuery })
        every { jpaQuery.offset(any()) } returns jpaQuery
        every { jpaQuery.limit(any()) } returns jpaQuery
        every { jpaQuery.orderBy(any()) } returns jpaQuery
        every { jpaQuery.fetch() } returns listOf(TestDTO(TEST_STRING))
        every { jpaQuery.fetchCount() } returns 1
    }

    @Test
    internal fun sort() {
        dataProvider.fetch(Query(0, 10, mutableListOf(AcrariumSort(QTestDTO.test.testValue, SortDirection.DESCENDING)) as List<QuerySortOrder>, null, null))
        verify { jpaQuery.orderBy(match { it.target == QTestDTO.test.testValue && !it.isAscending }) }
    }

    @Test
    internal fun filter() {
        val filter = mockk<() -> List<BooleanExpression>>(relaxed = true)
        every { filter.invoke() } returns emptyList()
        dataProvider.fetch(Query(0, 10, null, null, filter))
        verify { filter.invoke() }
    }

    @Test
    internal fun count() {
        expectThat(dataProvider.size(Query(0, 10, null, null, null))).isEqualTo(1)
    }

    companion object {
        private const val TEST_STRING = "TEST"
    }
}

data class TestDTO(val testValue: String)

class QTestDTO : EntityPathBase<TestDTO>(TestDTO::class.java, "test") {

    val testValue: StringPath = createString("testValue")

    companion object {
        val test = QTestDTO()
    }
}