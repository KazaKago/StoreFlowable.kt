package com.kazakago.storeflowable.core

import io.mockk.MockK
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Test

class StateContentTest {

    @Test
    fun validateWrapMethod() {
        val stateContent1 = StateContent.wrap(30)
        stateContent1 shouldBeInstanceOf StateContent.Exist::class
        val stateContent2 = StateContent.wrap(null)
        stateContent2 shouldBeInstanceOf StateContent.NotExist::class
    }

    @Test
    fun validateRawContentField() {
        val stateContent1 = StateContent.Exist(30)
        stateContent1.rawContent shouldBeEqualTo 30
        val stateContent2 = StateContent.Exist("Hello World!")
        stateContent2.rawContent shouldBeEqualTo "Hello World!"
    }

    @Test
    fun validateExistDoActionMethod() {
        val stateContent = StateContent.Exist(30)
        stateContent.doAction(
            onExist = {
                it shouldBeEqualTo 30
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
