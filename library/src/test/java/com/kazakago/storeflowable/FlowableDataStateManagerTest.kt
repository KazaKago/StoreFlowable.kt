package com.kazakago.storeflowable

import app.cash.turbine.test
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import kotlin.time.ExperimentalTime

class FlowableDataStateManagerTest {

    private lateinit var flowableDataStateManager: FlowableDataStateManager<String>

    @Before
    fun setup() {
        flowableDataStateManager = object : FlowableDataStateManager<String>() {}
    }

    @Test
    @ExperimentalTime
    fun flowSameKeyEvent() = runBlockingTest {
        var count = 0
        flowableDataStateManager.getFlow("hoge").test {
            when (count++) {
                0 -> {
                    expectItem() shouldBeInstanceOf DataState.Fixed::class
                }
                1 -> {
                    expectItem() shouldBeInstanceOf DataState.Loading::class
                }
                2 -> {
                    expectItem() shouldBeInstanceOf DataState.Fixed::class
                    expectComplete()
                }
                else -> fail()
            }
        }
        flowableDataStateManager.saveState("hoge", DataState.Loading())
        flowableDataStateManager.saveState("hoge", DataState.Error(mockk()))
    }

    @Test
    @ExperimentalTime
    fun flowDifferentKeyEvent() = runBlockingTest {
        var count = 0
        flowableDataStateManager.getFlow("hoge").test {
            when (count++) {
                0 -> expectItem() shouldBeInstanceOf DataState.Fixed::class
                1 -> expectNoEvents()
                else -> fail()
            }
        }
        flowableDataStateManager.saveState("hogehoge", DataState.Loading())
        flowableDataStateManager.saveState("hugahuga", DataState.Error(mockk()))
    }
}
