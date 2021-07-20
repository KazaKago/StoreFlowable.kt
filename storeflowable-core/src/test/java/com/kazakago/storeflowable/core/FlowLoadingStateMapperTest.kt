package com.kazakago.storeflowable.core

import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Assert.fail
import org.junit.Test

@ExperimentalCoroutinesApi
class FlowLoadingStateMapperTest {

    private val flowCompleted: FlowLoadingState<Int> = flowOf(LoadingState.Completed(30, mockk(), mockk()))

    @Test
    fun mapContent() = runBlockingTest {
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
