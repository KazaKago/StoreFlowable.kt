package com.kazakago.storeflowable.core

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Test

class LoadingStateTest {

    @Test
    fun doAction_Completed() {
        val state = LoadingState.Completed(10)
        state.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { content ->
                content shouldBeEqualTo 10
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun doAction_Loading() {
        val state = LoadingState.Loading<Int>(null)
        state.doAction(
            onLoading = {
                // ok
            },
            onCompleted = {
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun doAction_Error() {
        val state = LoadingState.Error<Int>(IllegalStateException())
        state.doAction(
            onLoading = {
                fail()
            },
            onCompleted = {
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }
}
