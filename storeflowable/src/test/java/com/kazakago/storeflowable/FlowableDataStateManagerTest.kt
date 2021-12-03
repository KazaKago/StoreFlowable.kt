package com.kazakago.storeflowable

import com.kazakago.storeflowable.datastate.DataState
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FlowableDataStateManagerTest {

    private lateinit var flowableDataStateManager: FlowableDataStateManager<String>

    @Before
    fun setup() {
        flowableDataStateManager = object : FlowableDataStateManager<String>() {}
    }

    @Test
    fun flowSameKeyEvent() = runTest {
        flowableDataStateManager.getFlow("hoge").toTest(this).use {
            delay(100)
            it.history.size shouldBeEqualTo 1
            it.history.last() shouldBeInstanceOf DataState.Fixed::class
            flowableDataStateManager.save("hoge", DataState.Loading())
            delay(100)
            it.history.size shouldBeEqualTo 2
            it.history.last() shouldBeInstanceOf DataState.Loading::class
            flowableDataStateManager.save("hoge", DataState.Error(mockk()))
            delay(100)
            it.history.size shouldBeEqualTo 3
            it.history.last() shouldBeInstanceOf DataState.Error::class
        }
    }

    @Test
    fun flowDifferentKeyEvent() = runTest {
        flowableDataStateManager.getFlow("hoge").toTest(this).use {
            delay(100)
            it.history.size shouldBeEqualTo 1
            it.history.last() shouldBeInstanceOf DataState.Fixed::class
            flowableDataStateManager.save("hogehoge", DataState.Loading())
            delay(100)
            it.history.size shouldBeEqualTo 1
            it.history.last() shouldBeInstanceOf DataState.Fixed::class
            flowableDataStateManager.save("hugahuga", DataState.Error(mockk()))
            delay(100)
            it.history.size shouldBeEqualTo 1
            it.history.last() shouldBeInstanceOf DataState.Fixed::class
        }
    }
}
