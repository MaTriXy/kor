/*
 * Copyright (C) 2018 Saúl Díaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sefford.kor.usecases

import com.sefford.kor.usecases.components.PerformanceModule
import com.sefford.kor.usecases.test.utils.TestError
import com.sefford.kor.usecases.test.utils.TestResponse
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * @author Saul Diaz <sefford@gmail.com>
 */
class UseCaseTest {

    @Before
    fun setUp() {
    }

    @Test
    fun `should execute correctly`() {
        val useCase = UseCase.Execute<TestError, TestResponse> { TestResponse() }
                .onError { TestError() }.build()

        assertThat(useCase.execute().isRight(), `is`(true))
    }

    @Test
    fun `should fail miserably`() {
        val useCase = UseCase.Execute<TestError, TestResponse> { throw IOException("Catastrophic fail") }
                .onError { TestError() }.build()

        assertThat(useCase.execute().isLeft(), `is`(true))
    }

    @Test
    fun `should properly fail in the postprocssor`() {
        UseCase.Execute<TestError, TestResponse> {
            val response = TestResponse()
            response.executed = true
            response
        }.process { response ->
            throw IOException("Catastrophic fail")
        }.persist { response ->
            response.persisted = true
            response
        }.onError { TestError(it) }.build()
                .execute().fold({
                    assertThat(it.exception, instanceOf(IOException::class.java))
                },
                        { fail() })
    }

    @Test
    fun `should execute all phases`() {
        UseCase.Execute<TestError, TestResponse> {
            val response = TestResponse()
            response.executed = true
            response
        }.process { response ->
            response.posprocessed = true
            response
        }.persist { response ->
            response.persisted = true
            response
        }.onError { TestError() }.build()
                .execute().fold({ fail() },
                        { response ->
                            assertThat(response.executed, `is`(true))
                            assertThat(response.posprocessed, `is`(true))
                            assertThat(response.persisted, `is`(true))
                        })
    }

    @Test
    fun `should call the performance module during correct execution`() {
        val performanceModule = TestPerformanceModule()

        UseCase.Execute<TestError, TestResponse> { TestResponse() }
                .onError { TestError() }
                .withIntrospection(performanceModule)
                .build()
                .execute()

        assertThat(performanceModule.metrics[START_METRIC], `is`(PERFORMANCE_METRIC))
        assertThat(performanceModule.metrics[END_METRIC], `is`(PERFORMANCE_METRIC))
    }

    @Test
    fun `should call the performance module during erroneous execution`() {
        val performanceModule = TestPerformanceModule()

        UseCase.Execute<TestError, TestResponse> { throw IOException("Catastrophic fail") }
                .onError { TestError() }
                .withIntrospection(performanceModule)
                .build()
                .execute()

        assertThat(performanceModule.metrics[START_METRIC], `is`(PERFORMANCE_METRIC))
        assertThat(performanceModule.metrics[END_METRIC], `is`(PERFORMANCE_METRIC))
    }

    class TestPerformanceModule : PerformanceModule {
        val metrics = mutableMapOf<String, String>()

        override val name: String
            get() = PERFORMANCE_METRIC

        override fun start(traceId: String) {
            metrics.put(START_METRIC, traceId)
        }

        override fun end(traceId: String) {
            metrics.put(END_METRIC, traceId)
        }

    }

    companion object {
        const val PERFORMANCE_METRIC = "Test"
        const val START_METRIC = "start"
        const val END_METRIC = "end"
    }
}
