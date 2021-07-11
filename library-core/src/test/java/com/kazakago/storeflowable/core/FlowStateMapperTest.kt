package com.kazakago.storeflowable.core

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Assert.fail
import org.junit.Test

@ExperimentalCoroutinesApi
class FlowStateMapperTest {

    private val flowCompleted: FlowableState<Int> = flowOf(State.Completed(30, appending = AdditionalState.Fixed(noMoreAdditionalData = true), prepending = AdditionalState.Fixed(noMoreAdditionalData = true)))

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
