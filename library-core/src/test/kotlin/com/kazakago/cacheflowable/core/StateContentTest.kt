package com.kazakago.cacheflowable.core

import io.mockk.MockK
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.fail
import org.junit.Test

class StateContentTest {

    @Test
    fun validateWrapMethod() {
        val stateContent1 = StateContent.wrap(30)
        assertThat(stateContent1, `is`(instanceOf(StateContent.Exist::class.java)))
        val stateContent2 = StateContent.wrap(null)
        assertThat(stateContent2, `is`(instanceOf(StateContent.NotExist::class.java)))
    }

    @Test
    fun validateRawContentField() {
        val stateContent1 = StateContent.Exist(30)
        assertThat(stateContent1.rawContent, `is`(30))
        val stateContent2 = StateContent.Exist("Hello World!")
        assertThat(stateContent2.rawContent, `is`("Hello World!"))
    }

    @Test
    fun validateExistDoActionMethod() {
        val stateContent = StateContent.Exist(30)
        stateContent.doAction(
            onExist = {
                assertThat(it, `is`(30))
            },
            onNotExist = {
                fail()
            }
        )
    }

    @Test
    fun validateNotExistDoActionMethod() {
        val stateContent = StateContent.NotExist<MockK>()
        stateContent.doAction(
            onExist = {
                fail()
            },
            onNotExist = {
                // ok
            }
        )
    }

}
