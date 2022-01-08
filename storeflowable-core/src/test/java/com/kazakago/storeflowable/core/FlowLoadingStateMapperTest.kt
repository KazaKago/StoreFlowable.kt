package com.kazakago.storeflowable.core

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.fail

@ExperimentalCoroutinesApi
class FlowLoadingStateMapperTest {

    private val flowCompleted: FlowLoadingState<Int> = flowOf(LoadingState.Completed(30, fakeAdditionalLoadingState(), fakeAdditionalLoadingState()))

    @Test
    fun mapContent() = runTest {
        val mappedFlowState = flowCompleted.mapContent { it + 70 }
        val mappedState = mappedFlowState.first()
        mappedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { content, _, _ ->
                content shouldBe 100
            },
            onError = {
                fail()
            }
        )
    }
}
