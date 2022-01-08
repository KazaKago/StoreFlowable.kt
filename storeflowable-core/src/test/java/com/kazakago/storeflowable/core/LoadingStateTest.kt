package com.kazakago.storeflowable.core

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test
import kotlin.test.fail

class LoadingStateTest {

    @Test
    fun doAction_Completed() {
        val state = LoadingState.Completed(10, fakeAdditionalLoadingState(), fakeAdditionalLoadingState())
        state.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { content, _, _ ->
                content shouldBe 10
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
                it.shouldBeTypeOf<IllegalStateException>()
            }
        )
    }
}
