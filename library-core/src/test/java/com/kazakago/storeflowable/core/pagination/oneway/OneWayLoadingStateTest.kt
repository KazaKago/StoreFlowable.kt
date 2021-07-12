package com.kazakago.storeflowable.core.pagination.oneway

import com.kazakago.storeflowable.core.pagination.AdditionalLoadingState
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Test

class OneWayLoadingStateTest {

    @Test
    fun doAction_Completed() {
        val state = OneWayLoadingState.Completed(10, appending = AdditionalLoadingState.Loading)
        state.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { content, appending ->
                content shouldBeEqualTo 10
                appending.doAction(
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
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun doAction_Loading() {
        val state = OneWayLoadingState.Loading<Int>(null)
        state.doAction(
            onLoading = {
                // ok
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
    fun doAction_Error() {
        val state = OneWayLoadingState.Error<Int>(IllegalStateException())
        state.doAction(
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
