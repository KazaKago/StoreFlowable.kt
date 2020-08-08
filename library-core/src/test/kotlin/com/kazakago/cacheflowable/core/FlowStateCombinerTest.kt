package com.kazakago.cacheflowable.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class FlowStateCombinerTest {

    private lateinit var flowFixedExist: Flow<State<Int>>
    private lateinit var flowFixedNotExist: Flow<State<Int>>
    private lateinit var flowLoadingExist: Flow<State<Int>>
    private lateinit var flowErrorExist: Flow<State<Int>>

    @Before
    fun setup() {
        flowFixedExist = flowOf(State.Fixed(StateContent.wrap(30)))
        flowFixedNotExist = flowOf(State.Fixed(StateContent.wrap(null)))
        flowLoadingExist = flowOf(State.Loading(StateContent.wrap(70)))
        flowErrorExist = flowOf(State.Error(StateContent.wrap(130), IllegalStateException()))
    }

    @Test
    fun validateCombineFixedLoadingMethod() = runBlocking {
        val combinedFlowState = flowFixedExist.combineState(flowLoadingExist) { value1, value2 ->
            assertThat(value1, `is`(30))
            assertThat(value2, `is`(70))
            value1 + value2
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onFixed = {
                fail()
            },
            onLoading = {
                // ok
            },
            onError = {
                fail()
            }
        )
        combinedState.content.doAction(
            onExist = {
                assertThat(it, `is`(100))
            },
            onNotExist = {
                fail()
            }
        )
    }

    @Test
    fun validateCombineFixedErrorMethod() = runBlocking {
        val combinedFlowState = flowFixedExist.combineState(flowErrorExist) { value1, value2 ->
            assertThat(value1, `is`(30))
            assertThat(value2, `is`(130))
            value1 + value2
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onFixed = {
                fail()
            },
            onLoading = {
                fail()
            },
            onError = {
                assertThat(it, `is`(instanceOf(IllegalStateException::class.java)))
            }
        )
        combinedState.content.doAction(
            onExist = {
                assertThat(it, `is`(160))
            },
            onNotExist = {
                fail()
            }
        )
    }

    @Test
    fun validateCombineLoadingErrorMethod() = runBlocking {
        val combinedFlowState = flowLoadingExist.combineState(flowErrorExist) { value1, value2 ->
            assertThat(value1, `is`(70))
            assertThat(value2, `is`(130))
            value1 + value2
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onFixed = {
                fail()
            },
            onLoading = {
                fail()
            },
            onError = {
                assertThat(it, `is`(instanceOf(IllegalStateException::class.java)))
            }
        )
        combinedState.content.doAction(
            onExist = {
                assertThat(it, `is`(200))
            },
            onNotExist = {
                fail()
            }
        )
    }

    @Test
    fun validateFixedFixedMethod() = runBlocking {
        val combinedFlowState = flowFixedExist.combineState(flowFixedNotExist) { value1, value2 ->
            fail()
            value1 + value2
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onFixed = {
                //ok
            },
            onLoading = {
                fail()
            },
            onError = {
                fail()
            }
        )
        combinedState.content.doAction(
            onExist = {
                fail()
            },
            onNotExist = {
                // ok
            }
        )
    }

}
