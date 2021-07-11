package com.kazakago.storeflowable.core

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeTrue
import org.junit.Assert.fail
import org.junit.Test

class StateTest {

    @Test
    fun doAction_Completed() {
        val state = State.Completed(10, appending = AdditionalState.Loading, prepending = AdditionalState.Fixed(noMoreAdditionalData = true))
        state.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { content, appending, prepending ->
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
                prepending.doAction(
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
        val state = State.Loading<Int>(null)
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
        val state = State.Error<Int>(IllegalStateException())
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
