package com.kazakago.storeflowable.core

import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import kotlin.test.Test
import kotlin.test.fail

@ExperimentalCoroutinesApi
class FlowLoadingStateMapperTest {

    private val flowCompleted: FlowLoadingState<Int> = flowOf(LoadingState.Completed(30, mockk(), mockk()))

    @Test
    fun mapContent() = runTest {
        val mappedFlowState = flowCompleted.mapContent { it + 70 }
        val mappedState = mappedFlowState.first()
        mappedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { content, _, _ ->
                content shouldBeEqualTo 100
            },
            onError = {
                fail()
            }
        )
    }
}
