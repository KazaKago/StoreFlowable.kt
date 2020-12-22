package com.kazakago.storeflowable

import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Before
import org.junit.Test

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
            flowableDataStateManager.saveState("hoge", DataState.Loading())
            it.history.size shouldBeEqualTo 2
            it.history[1] shouldBeInstanceOf DataState.Loading::class
            flowableDataStateManager.saveState("hoge", DataState.Error(mockk()))
            it.history.size shouldBeEqualTo 3
            it.history[2] shouldBeInstanceOf DataState.Error::class
        }
    }

    @Test
    fun flowDifferentKeyEvent() = runBlockingTest {
        flowableDataStateManager.getFlow("hoge").toTest(this).use {
            it.history.size shouldBeEqualTo 1
            it.history[0] shouldBeInstanceOf DataState.Fixed::class
            flowableDataStateManager.saveState("hogehoge", DataState.Loading())
            it.history.size shouldBeEqualTo 1
            flowableDataStateManager.saveState("hugahuga", DataState.Error(mockk()))
            it.history.size shouldBeEqualTo 1
        }
    }
}
