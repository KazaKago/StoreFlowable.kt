package com.kazakago.cacheflowable.core

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
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
            assertThat(value1, `is`(30))
            assertThat(value2, `is`(70))
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
                assertThat(it, `is`(100))
            },
            onNotExist = {
                fail()
            }
        )
    }

    @Test
    fun validateZipFixedErrorMethod() {
        val zippedState = fixedExistState.zip(errorExistState) { value1, value2 ->
            assertThat(value1, `is`(30))
            assertThat(value2, `is`(130))
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
                assertThat(it, `is`(instanceOf(IllegalStateException::class.java)))
            }
        )
        zippedState.content.doAction(
            onExist = {
                assertThat(it, `is`(160))
            },
            onNotExist = {
                fail()
            }
        )
    }

    @Test
    fun validateZipLoadingErrorMethod() {
        val zippedState = loadingExistState.zip(errorExistState) { value1, value2 ->
            assertThat(value1, `is`(70))
            assertThat(value2, `is`(130))
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
                assertThat(it, `is`(instanceOf(IllegalStateException::class.java)))
            }
        )
        zippedState.content.doAction(
            onExist = {
                assertThat(it, `is`(200))
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