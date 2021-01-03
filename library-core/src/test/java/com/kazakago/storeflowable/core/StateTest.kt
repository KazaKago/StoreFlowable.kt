package com.kazakago.storeflowable.core

import io.mockk.MockK
import io.mockk.mockk
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Test

class StateTest {

    @Test
    fun validateContentField() {
        val state1 = State.Fixed(StateContent.Exist<MockK>(mockk()))
        state1.content shouldBeInstanceOf StateContent.Exist::class
        val state2 = State.Fixed(StateContent.NotExist<MockK>())
        state2.content shouldBeInstanceOf StateContent.NotExist::class
    }

    @Test
    fun validateFixedDoActionMethod() {
        val state = State.Fixed<MockK>(mockk())
        state.doAction(
            onFixed = {
                // ok
            },
            onLoading = {
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun validateLoadingDoActionMethod() {
        val state = State.Loading<MockK>(mockk())
        state.doAction(
            onFixed = {
                fail()
            },
            onLoading = {
                // ok
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun validateErrorDoActionMethod() {
        val state = State.Error<MockK>(mockk(), IllegalStateException())
        state.doAction(
            onFixed = {
                fail()
            },
            onLoading = {
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }
}
