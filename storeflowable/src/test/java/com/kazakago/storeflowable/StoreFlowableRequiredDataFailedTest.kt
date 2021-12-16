package com.kazakago.storeflowable

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.coInvoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import java.net.UnknownHostException
import kotlin.test.Test

@ExperimentalCoroutinesApi
class StoreFlowableRequiredDataFailedTest {

    private enum class TestData(val needRefresh: Boolean) {
        ValidData(false),
        InvalidData(true),
        FetchedData(false),
    }

    private class TestFlowableFactory(private var dataCache: TestData?) : StoreFlowableFactory<Unit, TestData> {

        override val flowableDataStateManager: FlowableDataStateManager<Unit> = object : FlowableDataStateManager<Unit>() {}

        override suspend fun loadDataFromCache(param: Unit): TestData? {
            return dataCache
        }

        override suspend fun saveDataToCache(newData: TestData?, param: Unit) {
            dataCache = newData
        }

        override suspend fun fetchDataFromOrigin(param: Unit): TestData {
            throw UnknownHostException()
        }

        override suspend fun needRefresh(cachedData: TestData, param: Unit): Boolean {
            return cachedData.needRefresh
        }
    }

    @Test
    fun requiredData_Both_NoCache() = runTest {
        val storeFlowable = TestFlowableFactory(dataCache = null).create(Unit)
        coInvoking { storeFlowable.requireData(GettingFrom.Both) } shouldThrow UnknownHostException::class
    }

    @Test
    fun requiredData_Both_ValidCache() = runTest {
        val storeFlowable = TestFlowableFactory(dataCache = TestData.ValidData).create(Unit)
        storeFlowable.requireData(GettingFrom.Both) shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun requiredData_Both_InvalidCache() = runTest {
        val storeFlowable = TestFlowableFactory(dataCache = TestData.InvalidData).create(Unit)
        coInvoking { storeFlowable.requireData(GettingFrom.Both) } shouldThrow UnknownHostException::class
    }

    @Test
    fun requiredData_Cache_NoCache() = runTest {
        val storeFlowable = TestFlowableFactory(dataCache = null).create(Unit)
        coInvoking { storeFlowable.requireData(GettingFrom.Cache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun requiredData_Cache_ValidCache() = runTest {
        val storeFlowable = TestFlowableFactory(dataCache = TestData.ValidData).create(Unit)
        storeFlowable.requireData(GettingFrom.Cache) shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun requiredData_Cache_InvalidCache() = runTest {
        val storeFlowable = TestFlowableFactory(dataCache = TestData.InvalidData).create(Unit)
        coInvoking { storeFlowable.requireData(GettingFrom.Cache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun requiredData_Origin_NoCache() = runTest {
        val storeFlowable = TestFlowableFactory(dataCache = null).create(Unit)
        coInvoking { storeFlowable.requireData(GettingFrom.Origin) } shouldThrow UnknownHostException::class
    }

    @Test
    fun requiredData_Origin_ValidCache() = runTest {
        val storeFlowable = TestFlowableFactory(dataCache = TestData.ValidData).create(Unit)
        coInvoking { storeFlowable.requireData(GettingFrom.Origin) } shouldThrow UnknownHostException::class
    }

    @Test
    fun requiredData_Origin_InvalidCache() = runTest {
        val storeFlowable = TestFlowableFactory(dataCache = TestData.InvalidData).create(Unit)
        coInvoking { storeFlowable.requireData(GettingFrom.Origin) } shouldThrow UnknownHostException::class
    }
}