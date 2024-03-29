package com.kazakago.storeflowable.core

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.fail

@ExperimentalCoroutinesApi
class FlowLoadingStateCombinerTest {

    private val flowLoading: FlowLoadingState<Int> = flowOf(LoadingState.Loading(null))
    private val flowLoadingWithData: FlowLoadingState<Int> = flowOf(LoadingState.Loading(70))
    private val flowCompleted: FlowLoadingState<Int> = flowOf(LoadingState.Completed(30, AdditionalLoadingState.Fixed(true), AdditionalLoadingState.Fixed(true)))
    private val flowError: FlowLoadingState<Int> = flowOf(LoadingState.Error(IllegalStateException()))

    @Test
    fun combine_Loading_Loading() = runTest {
        val combinedFlowState = flowLoading.combineState(flowLoading) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBe null
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_Loading_LoadingWithData() = runTest {
        val combinedFlowState = flowLoading.combineState(flowLoadingWithData) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBe null
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_Loading_Completed() = runTest {
        val combinedFlowState = flowLoading.combineState(flowCompleted) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBe null
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_Loading_Error() = runTest {
        val combinedFlowState = flowLoading.combineState(flowError) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                it.shouldBeTypeOf<IllegalStateException>()
            }
        )
    }

    @Test
    fun combine_LoadingWithData_Loading() = runTest {
        val combinedFlowState = flowLoadingWithData.combineState(flowLoading) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBe null
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_LoadingWithData_LoadingWithData() = runTest {
        val combinedFlowState = flowLoadingWithData.combineState(flowLoadingWithData) { value1, value2 ->
            value1 shouldBe 70
            value2 shouldBe 70
            value1 + value2
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBe 140
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_LoadingWithData_Completed() = runTest {
        val combinedFlowState = flowLoadingWithData.combineState(flowCompleted) { value1, value2 ->
            value1 shouldBe 70
            value2 shouldBe 30
            value1 + value2
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBe 100
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_LoadingWithData_Error() = runTest {
        val combinedFlowState = flowLoadingWithData.combineState(flowError) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                it.shouldBeTypeOf<IllegalStateException>()
            }
        )
    }

    @Test
    fun combine_Completed_Loading() = runTest {
        val combinedFlowState = flowCompleted.combineState(flowLoading) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBe null
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_Completed_LoadingWithData() = runTest {
        val combinedFlowState = flowCompleted.combineState(flowLoadingWithData) { value1, value2 ->
            value1 shouldBe 30
            value2 shouldBe 70
            value1 + value2
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                it shouldBe 100
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_Completed_Completed() = runTest {
        val combinedFlowState = flowCompleted.combineState(flowCompleted) { value1, value2 ->
            value1 shouldBe 30
            value2 shouldBe 30
            value1 + value2
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { content, _, _ ->
                content shouldBe 60
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun combine_Completed_Error() = runTest {
        val combinedFlowState = flowCompleted.combineState(flowError) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                it.shouldBeTypeOf<IllegalStateException>()
            }
        )
    }

    @Test
    fun combine_Error_Loading() = runTest {
        val combinedFlowState = flowError.combineState(flowLoading) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                it.shouldBeTypeOf<IllegalStateException>()
            }
        )
    }

    @Test
    fun combine_Error_LoadingWithData() = runTest {
        val combinedFlowState = flowError.combineState(flowLoadingWithData) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                it.shouldBeTypeOf<IllegalStateException>()
            }
        )
    }

    @Test
    fun combine_Error_Completed() = runTest {
        val combinedFlowState = flowError.combineState(flowCompleted) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                it.shouldBeTypeOf<IllegalStateException>()
            }
        )
    }

    @Test
    fun combine_Error_Error() = runTest {
        val combinedFlowState = flowError.combineState(flowError) { _, _ ->
            fail()
        }
        val combinedState = combinedFlowState.first()
        combinedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                it.shouldBeTypeOf<IllegalStateException>()
            }
        )
    }
}
