package com.kazakago.storeflowable.core

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
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
    fun combineWithFixedLoading() = runBlockingTest {
        val combinedFlowState = flowFixedExist.combineState(flowLoadingExist) { value1, value2 ->
            value1 shouldBeEqualTo 30
            value2 shouldBeEqualTo 70
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
                it shouldBeEqualTo 100
            },
            onNotExist = {
                fail()
            }
        )
    }

    @Test
    fun combineWithFixedError() = runBlockingTest {
        val combinedFlowState = flowFixedExist.combineState(flowErrorExist) { value1, value2 ->
            value1 shouldBeEqualTo 30
            value2 shouldBeEqualTo 130
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
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
        combinedState.content.doAction(
            onExist = {
                it shouldBeEqualTo 160
            },
            onNotExist = {
                fail()
            }
        )
    }

    @Test
    fun combineWithLoadingError() = runBlockingTest {
        val combinedFlowState = flowLoadingExist.combineState(flowErrorExist) { value1, value2 ->
            value1 shouldBeEqualTo 70
            value2 shouldBeEqualTo 130
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
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
        combinedState.content.doAction(
            onExist = {
                it shouldBeEqualTo 200
            },
            onNotExist = {
                fail()
            }
        )
    }

    @Test
    fun combineWithFixedFixed() = runBlockingTest {
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
