package com.kazakago.storeflowable.core

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import kotlin.test.Test
import kotlin.test.fail

@ExperimentalCoroutinesApi
class LoadingStateZipperTest {

    private val loading: LoadingState<Int> = LoadingState.Loading(null)
    private val loadingWithData: LoadingState<Int> = LoadingState.Loading(70)
    private val completed: LoadingState<Int> = LoadingState.Completed(30, AdditionalLoadingState.Fixed(true), AdditionalLoadingState.Fixed(true))
    private val error: LoadingState<Int> = LoadingState.Error(IllegalStateException())

    @Test
    fun zip_Loading_Loading() = runTest {
        val zippedState = loading.zip(loading) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                it.shouldBeNull()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_Loading_LoadingWithData() = runTest {
        val zippedState = loading.zip(loadingWithData) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                it.shouldBeNull()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_Loading_Completed() = runTest {
        val zippedState = loading.zip(completed) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                it.shouldBeNull()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_Loading_Error() = runTest {
        val zippedState = loading.zip(error) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun zip_LoadingWithData_Loading() = runTest {
        val zippedState = loadingWithData.zip(loading) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                it.shouldBeNull()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_LoadingWithData_LoadingWithData() = runTest {
        val zippedState = loadingWithData.zip(loadingWithData) { value1, value2 ->
            value1 shouldBeEqualTo 70
            value2 shouldBeEqualTo 70
            value1 + value2
        }
        zippedState.doAction(
            onLoading = {
                it shouldBeEqualTo 140
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_LoadingWithData_Completed() = runTest {
        val zippedState = loadingWithData.zip(completed) { value1, value2 ->
            value1 shouldBeEqualTo 70
            value2 shouldBeEqualTo 30
            value1 + value2
        }
        zippedState.doAction(
            onLoading = {
                it shouldBeEqualTo 100
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_LoadingWithData_Error() = runTest {
        val zippedState = loadingWithData.zip(error) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun zip_Completed_Loading() = runTest {
        val zippedState = completed.zip(loading) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                it.shouldBeNull()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_Completed_LoadingWithData() = runTest {
        val zippedState = completed.zip(loadingWithData) { value1, value2 ->
            value1 shouldBeEqualTo 30
            value2 shouldBeEqualTo 70
            value1 + value2
        }
        zippedState.doAction(
            onLoading = {
                it shouldBeEqualTo 100
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_Completed_Completed() = runTest {
        val zippedState = completed.zip(completed) { value1, value2 ->
            value1 shouldBeEqualTo 30
            value2 shouldBeEqualTo 30
            value1 + value2
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { content, _, _ ->
                content shouldBeEqualTo 60
            },
            onError = {
                fail()
            }
        )
    }

    @Test
    fun zip_Completed_Error() = runTest {
        val zippedState = completed.zip(error) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun zip_Error_Loading() = runTest {
        val zippedState = error.zip(loading) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun zip_Error_LoadingWithData() = runTest {
        val zippedState = error.zip(loadingWithData) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun zip_Error_Completed() = runTest {
        val zippedState = error.zip(completed) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }

    @Test
    fun zip_Error_Error() = runTest {
        val zippedState = error.zip(error) { _, _ ->
            fail()
        }
        zippedState.doAction(
            onLoading = {
                fail()
            },
            onCompleted = { _, _, _ ->
                fail()
            },
            onError = {
                it shouldBeInstanceOf IllegalStateException::class
            }
        )
    }
}
