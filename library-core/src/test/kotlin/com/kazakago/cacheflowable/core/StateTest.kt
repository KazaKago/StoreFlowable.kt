package com.kazakago.cacheflowable.core

import io.mockk.MockK
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.fail
import org.junit.Test

class StateTest {

    @Test
    fun validateContentField() {
        val state1 = State.Fixed(StateContent.Exist<MockK>(mockk()))
        assertThat(state1.content, `is`(instanceOf(StateContent.Exist::class.java)))
        val state2 = State.Fixed(StateContent.NotExist<MockK>())
        assertThat(state2.content, `is`(instanceOf(StateContent.NotExist::class.java)))
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
                assertThat(it, `is`(instanceOf(IllegalStateException::class.java)))
            }
        )
    }
}
