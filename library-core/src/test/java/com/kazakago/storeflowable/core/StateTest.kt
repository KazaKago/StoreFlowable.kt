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
        val stateContent = State.Fixed<MockK>(mockk())
        stateContent.doAction(
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
        val stateContent = State.Loading<MockK>(mockk())
        stateContent.doAction(
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
        val stateContent = State.Error<MockK>(mockk(), IllegalStateException())
        stateContent.doAction(
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
