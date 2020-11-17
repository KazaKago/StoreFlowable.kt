package com.kazakago.storeflowable.core

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class StateContentZipperTest {

    private lateinit var exist1: StateContent<Int>
    private lateinit var exist2: StateContent<Int>
    private lateinit var notExist1: StateContent<Int>
    private lateinit var notExist2: StateContent<Int>

    @Before
    fun setup() {
        exist1 = StateContent.wrap(30)
        exist2 = StateContent.wrap(70)
        notExist1 = StateContent.wrap(null)
        notExist2 = StateContent.wrap(null)
    }

    @Test
    fun validateZipExistExistMethod() {
        val zippedContent = exist1.zip(exist2) { value1, value2 ->
            value1 shouldBeEqualTo 30
            value2 shouldBeEqualTo 70
            value1 + value2
        }
        zippedContent.doAction(
            onExist = {
                it shouldBeEqualTo 100
            },
            onNotExist = {
                fail()
            }
        )
    }

    @Test
    fun validateZipExistNotExistMethod() {
        val zippedContent = exist1.zip(notExist1) { value1, value2 ->
            fail()
            value1 + value2
        }
        zippedContent.doAction(
            onExist = {
                fail()
            },
            onNotExist = {
                // ok
            }
        )
    }

    @Test
    fun validateZipNotExistNotExistMethod() {
        val zippedContent = notExist1.zip(notExist2) { value1, value2 ->
            fail()
            value1 + value2
        }
        zippedContent.doAction(
            onExist = {
                fail()
            },
            onNotExist = {
                // ok
            }
        )
    }

}
