package com.kazakago.storeflowable.core

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class StateZipperTest {

    private lateinit var fixedExistState: State<Int>
    private lateinit var loadingExistState: State<Int>
    private lateinit var errorExistState: State<Int>
    private lateinit var fixedNotExistState: State<Int>

    @Before
    fun setup() {
        fixedExistState = State.Fixed(StateContent.wrap(30))
        loadingExistState = State.Loading(StateContent.wrap(70))
        errorExistState = State.Error(StateContent.wrap(130), IllegalStateException())
        fixedNotExistState = State.Fixed(StateContent.wrap(null))
    }

    @Test
    fun validateZipFixedLoadingMethod() {
        val zippedState = fixedExistState.zip(loadingExistState) { value1, value2 ->
            value1 shouldBeEqualTo 30
            value2 shouldBeEqualTo 70
            value1 + value2
        }
        zippedState.doAction(
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
        zippedState.content.doAction(
            onExist = {
                it shouldBeEqualTo 100
            },
            onNotExist = {
                fail()
            }
        )
    }

    @Test
    fun validateZipFixedErrorMethod() {
        val zippedState = fixedExistState.zip(errorExistState) { value1, value2 ->
            value1 shouldBeEqualTo 30
            value2 shouldBeEqualTo 130
            value1 + value2
        }
        zippedState.doAction(
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
        zippedState.content.doAction(
            onExist = {
                it shouldBeEqualTo 160
            },
            onNotExist = {
                fail()
            }
        )
    }

    @Test
    fun validateZipLoadingErrorMethod() {
        val zippedState = loadingExistState.zip(errorExistState) { value1, value2 ->
            value1 shouldBeEqualTo 70
            value2 shouldBeEqualTo 130
            value1 + value2
        }
        zippedState.doAction(
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
        zippedState.content.doAction(
            onExist = {
                it shouldBeEqualTo 200
            },
            onNotExist = {
                fail()
            }
        )
    }

    @Test
    fun validateZipFixedFixedNotExistMethod() {
        val zippedState = fixedExistState.zip(fixedNotExistState) { value1, value2 ->
            fail()
            value1 + value2
        }
        zippedState.doAction(
            onFixed = {
                //ok
            },
            onLoading = {
                fail()
            },
            onError = {
                fail()
            }
        )
        zippedState.content.doAction(
            onExist = {
                fail()
            },
            onNotExist = {
                // ok
            }
        )
    }
}
