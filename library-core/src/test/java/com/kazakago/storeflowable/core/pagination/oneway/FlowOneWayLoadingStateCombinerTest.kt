package com.kazakago.storeflowable.core.pagination.oneway

import com.kazakago.storeflowable.core.pagination.AdditionalLoadingState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Test

@ExperimentalCoroutinesApi
class FlowOneWayLoadingStateCombinerTest {

    private val flowLoading: FlowableOneWayLoadingState<Int> = flowOf(OneWayLoadingState.Loading(null))
    private val flowLoadingWithData: FlowableOneWayLoadingState<Int> = flowOf(OneWayLoadingState.Loading(70))
    private val flowCompleted: FlowableOneWayLoadingState<Int> = flowOf(OneWayLoadingState.Completed(30, appending = AdditionalLoadingState.Fixed(noMoreAdditionalData = false)))
    private val flowError: FlowableOneWayLoadingState<Int> = flowOf(OneWayLoadingState.Error(IllegalStateException()))

    @Test
    fun combine_Loading_Loading() = runBlockingTest {
        val combinedFlowState = flowLoading.combineState(flowLoading) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBeEqualTo null
            },
            onCompleted = { _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_Loading_LoadingWithData() = runBlockingTest {
        val combinedFlowState = flowLoading.combineState(flowLoadingWithData) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBeEqualTo null
            },
            onCompleted = { _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_Loading_Completed() = runBlockingTest {
        val combinedFlowState = flowLoading.combineState(flowCompleted) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBeEqualTo null
            },
            onCompleted = { _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_Loading_Error() = runBlockingTest {
        val combinedFlowState = flowLoading.combineState(flowError) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _ ->
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun combine_LoadingWithData_Loading() = runBlockingTest {
        val combinedFlowState = flowLoadingWithData.combineState(flowLoading) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBeEqualTo null
            },
            onCompleted = { _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_LoadingWithData_LoadingWithData() = runBlockingTest {
        val combinedFlowState = flowLoadingWithData.combineState(flowLoadingWithData) { value1, value2 ->
            value1 shouldBeEqualTo 70
            value2 shouldBeEqualTo 70
            value1 + value2
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBeEqualTo 140
            },
            onCompleted = { _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_LoadingWithData_Completed() = runBlockingTest {
        val combinedFlowState = flowLoadingWithData.combineState(flowCompleted) { value1, value2 ->
            value1 shouldBeEqualTo 70
            value2 shouldBeEqualTo 30
            value1 + value2
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBeEqualTo 100
            },
            onCompleted = { _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_LoadingWithData_Error() = runBlockingTest {
        val combinedFlowState = flowLoadingWithData.combineState(flowError) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _ ->
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun combine_Completed_Loading() = runBlockingTest {
        val combinedFlowState = flowCompleted.combineState(flowLoading) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBeEqualTo null
            },
            onCompleted = { _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_Completed_LoadingWithData() = runBlockingTest {
        val combinedFlowState = flowCompleted.combineState(flowLoadingWithData) { value1, value2 ->
            value1 shouldBeEqualTo 30
            value2 shouldBeEqualTo 70
            value1 + value2
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBeEqualTo 100
            },
            onCompleted = { _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_Completed_Completed() = runBlockingTest {
        val combinedFlowState = flowCompleted.combineState(flowCompleted) { value1, value2 ->
            value1 shouldBeEqualTo 30
            value2 shouldBeEqualTo 30
            value1 + value2
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { content, _ ->
                content shouldBeEqualTo 60
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_Completed_Error() = runBlockingTest {
        val combinedFlowState = flowCompleted.combineState(flowError) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _ ->
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun combine_Error_Loading() = runBlockingTest {
        val combinedFlowState = flowError.combineState(flowLoading) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _ ->
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun combine_Error_LoadingWithData() = runBlockingTest {
        val combinedFlowState = flowError.combineState(flowLoadingWithData) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _ ->
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun combine_Error_Completed() = runBlockingTest {
        val combinedFlowState = flowError.combineState(flowCompleted) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _ ->
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun combine_Error_Error() = runBlockingTest {
        val combinedFlowState = flowError.combineState(flowError) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _ ->
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }
}
