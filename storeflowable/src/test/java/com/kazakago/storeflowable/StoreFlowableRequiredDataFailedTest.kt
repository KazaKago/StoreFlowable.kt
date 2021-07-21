package com.kazakago.storeflowable

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.coInvoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.Test
import java.net.UnknownHostException

@ExperimentalCoroutinesApi
class StoreFlowableRequiredDataFailedTest {

    private enum class TestData(val needRefresh: Boolean) {
        ValidData(false),
        InvalidData(true),
        FetchedData(false),
    }

    private class TestFlowableFactory(private var dataCache: TestData?) : StoreFlowableFactory<String, TestData> {

        override val key: String = "Key"

        override val flowableDataStateManager: FlowableDataStateManager<String> = object : FlowableDataStateManager<String>() {}

        override suspend fun loadDataFromCache(): TestData? {
            return dataCache
        }

        override suspend fun saveDataToCache(newData: TestData?) {
            dataCache = newData
        }

        override suspend fun fetchDataFromOrigin(): TestData {
            throw UnknownHostException()
        }

        override suspend fun needRefresh(cachedData: TestData): Boolean {
            return cachedData.needRefresh
        }
    }

    @Test
    fun requiredData_Both_NoCache() = runBlockingTest {
        val storeFlowable = TestFlowableFactory(dataCache = null).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Both) } shouldThrow UnknownHostException::class
    }

    @Test
    fun requiredData_Both_ValidCache() = runBlockingTest {
        val storeFlowable = TestFlowableFactory(dataCache = TestData.ValidData).create()
        storeFlowable.requireData(GettingFrom.Both) shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun requiredData_Both_InvalidCache() = runBlockingTest {
        val storeFlowable = TestFlowableFactory(dataCache = TestData.InvalidData).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Both) } shouldThrow UnknownHostException::class
    }

    @Test
    fun requiredData_Cache_NoCache() = runBlockingTest {
        val storeFlowable = TestFlowableFactory(dataCache = null).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Cache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun requiredData_Cache_ValidCache() = runBlockingTest {
        val storeFlowable = TestFlowableFactory(dataCache = TestData.ValidData).create()
        storeFlowable.requireData(GettingFrom.Cache) shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun requiredData_Cache_InvalidCache() = runBlockingTest {
        val storeFlowable = TestFlowableFactory(dataCache = TestData.InvalidData).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Cache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun requiredData_Origin_NoCache() = runBlockingTest {
        val storeFlowable = TestFlowableFactory(dataCache = null).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Origin) } shouldThrow UnknownHostException::class
    }

    @Test
    fun requiredData_Origin_ValidCache() = runBlockingTest {
        val storeFlowable = TestFlowableFactory(dataCache = TestData.ValidData).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Origin) } shouldThrow UnknownHostException::class
    }

    @Test
    fun requiredData_Origin_InvalidCache() = runBlockingTest {
        val storeFlowable = TestFlowableFactory(dataCache = TestData.InvalidData).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Origin) } shouldThrow UnknownHostException::class
    }
}
