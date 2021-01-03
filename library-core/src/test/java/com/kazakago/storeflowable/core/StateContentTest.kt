package com.kazakago.storeflowable.core

import io.mockk.MockK
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Test

class StateContentTest {

    @Test
    fun wrap() {
        val content1 = StateContent.wrap(30)
        content1 shouldBeInstanceOf StateContent.Exist::class
        val content2 = StateContent.wrap(null)
        content2 shouldBeInstanceOf StateContent.NotExist::class
    }

    @Test
    fun rawContent() {
        val content1 = StateContent.Exist(30)
        content1.rawContent shouldBeEqualTo 30
        val content2 = StateContent.Exist("Hello World!")
        content2.rawContent shouldBeEqualTo "Hello World!"
    }

    @Test
    fun doActionWithExist() {
        val content = StateContent.Exist(30)
        content.doAction(
            onExist = {
                it shouldBeEqualTo 30
            },
            onNotExist = {
                fail()
            }
        )
    }

    @Test
    fun doActionNotExist() {
        val content = StateContent.NotExist<MockK>()
        content.doAction(
            onExist = {
                fail()
            },
            onNotExist = {
                // ok
            }
        )
    }
}
