package com.kazakago.storeflowable.core

import io.mockk.mockk
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import kotlin.test.Test
import kotlin.test.fail

class LoadingStateTest {

    @Test
    fun doAction_Completed() {
        val state = LoadingState.Completed(10, mockk(), mockk())
        state.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { content, _, _ ->
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
        val state = LoadingState.Error<Int>(IllegalStateException())
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
