package com.kazakago.storeflowable

import com.kazakago.storeflowable.datastate.DataState
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
class FlowableDataStateManagerTest {

    private lateinit var flowableDataStateManager: FlowableDataStateManager<String>

    @BeforeTest
    fun setup() {
        flowableDataStateManager = object : FlowableDataStateManager<String>() {}
    }

    @Test
    fun flowSameKeyEvent() = runTest {
        flowableDataStateManager.getFlow("hoge").toTest(this).use {
            delay(100)
            it.history.size shouldBe 1
            it.history.last().shouldBeTypeOf<DataState.Fixed>()
            flowableDataStateManager.save("hoge", DataState.Loading())
            delay(100)
            it.history.size shouldBe 2
            it.history.last().shouldBeTypeOf<DataState.Loading>()
            flowableDataStateManager.save("hoge", DataState.Error(fakeException()))
            delay(100)
            it.history.size shouldBe 3
            it.history.last().shouldBeTypeOf<DataState.Error>()
        }
    }

    @Test
    fun flowDifferentKeyEvent() = runTest {
        flowableDataStateManager.getFlow("hoge").toTest(this).use {
            delay(100)
            it.history.size shouldBe 1
            it.history.last().shouldBeTypeOf<DataState.Fixed>()
            flowableDataStateManager.save("hogehoge", DataState.Loading())
            delay(100)
            it.history.size shouldBe 1
            it.history.last().shouldBeTypeOf<DataState.Fixed>()
            flowableDataStateManager.save("hugahuga", DataState.Error(fakeException()))
            delay(100)
            it.history.size shouldBe 1
            it.history.last().shouldBeTypeOf<DataState.Fixed>()
        }
    }
}
