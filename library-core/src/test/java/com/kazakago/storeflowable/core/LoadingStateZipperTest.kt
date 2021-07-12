package com.kazakago.storeflowable.core

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Test

@ExperimentalCoroutinesApi
class LoadingStateZipperTest {

    private val loading: LoadingState<Int> = LoadingState.Loading(null)
    private val loadingWithData: LoadingState<Int> = LoadingState.Loading(70)
    private val completed: LoadingState<Int> = LoadingState.Completed(30)
    private val error: LoadingState<Int> = LoadingState.Error(IllegalStateException())

    @Test
    fun zip_Loading_Loading() = runBlockingTest {
        val zippedState = loading.zip(loading) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                it shouldBeEqualTo null
            },
            onCompleted = {
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_Loading_LoadingWithData() = runBlockingTest {
        val zippedState = loading.zip(loadingWithData) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                it shouldBeEqualTo null
            },
            onCompleted = {
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_Loading_Completed() = runBlockingTest {
        val zippedState = loading.zip(completed) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                it shouldBeEqualTo null
            },
            onCompleted = {
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_Loading_Error() = runBlockingTest {
        val zippedState = loading.zip(error) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = {
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun zip_LoadingWithData_Loading() = runBlockingTest {
        val zippedState = loadingWithData.zip(loading) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                it shouldBeEqualTo null
            },
            onCompleted = {
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_LoadingWithData_LoadingWithData() = runBlockingTest {
        val zippedState = loadingWithData.zip(loadingWithData) { value1, value2 ->
            value1 shouldBeEqualTo 70
            value2 shouldBeEqualTo 70
            value1 + value2
        }
        zippedState.doAction(
            onLoading = {
                it shouldBeEqualTo 140
            },
            onCompleted = {
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_LoadingWithData_Completed() = runBlockingTest {
        val zippedState = loadingWithData.zip(completed) { value1, value2 ->
            value1 shouldBeEqualTo 70
            value2 shouldBeEqualTo 30
            value1 + value2
        }
        zippedState.doAction(
            onLoading = {
                it shouldBeEqualTo 100
            },
            onCompleted = {
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_LoadingWithData_Error() = runBlockingTest {
        val zippedState = loadingWithData.zip(error) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = {
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun zip_Completed_Loading() = runBlockingTest {
        val zippedState = completed.zip(loading) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                it shouldBeEqualTo null
            },
            onCompleted = {
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_Completed_LoadingWithData() = runBlockingTest {
        val zippedState = completed.zip(loadingWithData) { value1, value2 ->
            value1 shouldBeEqualTo 30
            value2 shouldBeEqualTo 70
            value1 + value2
        }
        zippedState.doAction(
            onLoading = {
                it shouldBeEqualTo 100
            },
            onCompleted = {
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_Completed_Completed() = runBlockingTest {
        val zippedState = completed.zip(completed) { value1, value2 ->
            value1 shouldBeEqualTo 30
            value2 shouldBeEqualTo 30
            value1 + value2
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = {
                it shouldBeEqualTo 60
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_Completed_Error() = runBlockingTest {
        val zippedState = completed.zip(error) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = {
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun zip_Error_Loading() = runBlockingTest {
        val zippedState = error.zip(loading) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = {
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun zip_Error_LoadingWithData() = runBlockingTest {
        val zippedState = error.zip(loadingWithData) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = {
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun zip_Error_Completed() = runBlockingTest {
        val zippedState = error.zip(completed) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = {
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun zip_Error_Error() = runBlockingTest {
        val zippedState = error.zip(error) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = {
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }
}
