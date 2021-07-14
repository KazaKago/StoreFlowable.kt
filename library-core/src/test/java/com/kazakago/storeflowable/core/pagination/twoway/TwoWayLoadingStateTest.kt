package com.kazakago.storeflowable.core.pagination.twoway

import com.kazakago.storeflowable.core.pagination.AdditionalLoadingState
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeTrue
import org.junit.Assert.fail
import org.junit.Test

class TwoWayLoadingStateTest {

    @Test
    fun doAction_Completed() {
        val state = TwoWayLoadingState.Completed(10, next = AdditionalLoadingState.Loading, prev = AdditionalLoadingState.Fixed(noMoreAdditionalData = true))
        state.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { content, next, prev ->
                content shouldBeEqualTo 10
                next.doAction(
                    onLoading = {
                        // ok
                    },
                    onFixed = {
                        fail()
                    },
                    onError = {
                        fail()
                    }
                )
                prev.doAction(
                    onLoading = {
                        fail()
                    },
                    onFixed = {
                        it.shouldBeTrue()
                    },
                    onError = {
                        fail()
                    }
                )
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun doAction_Loading() {
        val state = TwoWayLoadingState.Loading<Int>(null)
        state.doAction(
            onLoading = {
                // ok
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
    fun doAction_Error() {
        val state = TwoWayLoadingState.Error<Int>(IllegalStateException())
        state.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }
}
