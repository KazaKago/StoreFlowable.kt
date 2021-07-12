package com.kazakago.storeflowable.datastate

import com.kazakago.storeflowable.toTest
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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
    fun flowSameKeyEvent() = runBlockingTest {
        flowableDataStateManager.getFlow("hoge").toTest(this).use {
            it.history.size shouldBeEqualTo 1
            it.history[0] shouldBeInstanceOf DataState.Fixed::class
            flowableDataStateManager.save("hoge", DataState.Loading())
            it.history.size shouldBeEqualTo 2
            it.history[1] shouldBeInstanceOf DataState.Loading::class
            flowableDataStateManager.save("hoge", DataState.Error(mockk()))
            it.history.size shouldBeEqualTo 3
            it.history[2] shouldBeInstanceOf DataState.Error::class
        }
    }

    @Test
    fun flowDifferentKeyEvent() = runBlockingTest {
        flowableDataStateManager.getFlow("hoge").toTest(this).use {
            it.history.size shouldBeEqualTo 1
            it.history[0] shouldBeInstanceOf DataState.Fixed::class
            flowableDataStateManager.save("hogehoge", DataState.Loading())
            it.history.size shouldBeEqualTo 1
            flowableDataStateManager.save("hugahuga", DataState.Error(mockk()))
            it.history.size shouldBeEqualTo 1
        }
    }
}
