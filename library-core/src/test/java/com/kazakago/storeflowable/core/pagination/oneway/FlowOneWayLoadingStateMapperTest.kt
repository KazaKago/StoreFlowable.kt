package com.kazakago.storeflowable.core.pagination.oneway

import com.kazakago.storeflowable.core.pagination.AdditionalLoadingState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Assert.fail
import org.junit.Test

@ExperimentalCoroutinesApi
class FlowOneWayLoadingStateMapperTest {

    private val flowCompleted: FlowableOneWayLoadingState<Int> = flowOf(OneWayLoadingState.Completed(30, next = AdditionalLoadingState.Fixed(noMoreAdditionalData = false)))

    @Test
    fun mapContent() = runBlockingTest {
        val mappedFlowState = flowCompleted.mapContent { it + 70 }
        val mappedState = mappedFlowState.first()
        mappedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { content, _ ->
                content shouldBeEqualTo 100
            },
            onError = {
                fail()
            }
        )
    }
}
