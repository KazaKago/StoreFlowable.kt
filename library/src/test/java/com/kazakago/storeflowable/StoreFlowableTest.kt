package com.kazakago.storeflowable

import com.kazakago.storeflowable.datastate.FlowableDataStateManager
import com.kazakago.storeflowable.origin.GettingFrom
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.coInvoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.Test
import java.net.UnknownHostException

@ExperimentalCoroutinesApi
class StoreFlowableTest {

    enum class TestData(val needRefresh: Boolean) {
        ValidData(false),
        InvalidData(true),
        FetchedData(false),
    }

    private abstract class TestFlowableFactory(private var dataCache: TestData?) : StoreFlowableFactory<String, TestData> {

        override val key: String = "Key"

        override val flowableDataStateManager: FlowableDataStateManager<String> = object : FlowableDataStateManager<String>() {}

        override suspend fun loadDataFromCache(): TestData? {
            return dataCache
        }

        override suspend fun saveDataToCache(newData: TestData?) {
            dataCache = newData
        }

        override suspend fun needRefresh(cachedData: TestData): Boolean {
            return cachedData.needRefresh
        }
    }

    private class SucceedTestFlowableFactory(dataCache: TestData?) : TestFlowableFactory(dataCache) {

        override suspend fun fetchDataFromOrigin(): TestData {
            return TestData.FetchedData
        }
    }

    private class FailedTestFlowableFactory(dataCache: TestData?) : TestFlowableFactory(dataCache) {

        override suspend fun fetchDataFromOrigin(): TestData {
            throw UnknownHostException()
        }
    }

//    @Test
//    fun flowWithNoCache() = runBlockingTest {
//        SucceedTestFlowableFactory(dataCache = null).create().publish().toTest(this).use {
//            delay(100)
//            it.history.size shouldBeEqualTo 2
//            it.history[0].let { state ->
//                state shouldBeInstanceOf State.Loading::class
//            }
//            it.history[1].let { state ->
//                state shouldBeInstanceOf State.Completed::class
//                (state as State.Completed).content shouldBeEqualTo TestData.FetchedData
//            }
//        }
//    }
//
//    @Test
//    fun flowWithValidCache() = runBlockingTest {
//        SucceedTestFlowableFactory(dataCache = TestData.ValidData).create().publish().toTest(this).use {
//            delay(100)
//            it.history.size shouldBeEqualTo 1
//            it.history[0].let { state ->
//                state shouldBeInstanceOf State.Completed::class
//                (state as State.Completed).content shouldBeEqualTo TestData.ValidData
//            }
//        }
//    }
//
//    @Test
//    fun flowWithInvalidCache() = runBlockingTest {
//        SucceedTestFlowableFactory(dataCache = TestData.InvalidData).create().publish().toTest(this).use {
//            delay(100)
//            it.history.size shouldBeEqualTo 2
//            it.history[0].let { state ->
//                state shouldBeInstanceOf State.Loading::class
//            }
//            it.history[1].let { state ->
//                state shouldBeInstanceOf State.Completed::class
//                (state as State.Completed).content shouldBeEqualTo TestData.FetchedData
//            }
//        }
//    }
//
//    @Test
//    fun flowFailedWithNoCache() = runBlockingTest {
//        FailedTestFlowableFactory(dataCache = null).create().publish().toTest(this).use {
//            delay(100)
//            it.history.size shouldBeEqualTo 2
//            it.history[0].let { state ->
//                state shouldBeInstanceOf State.Loading::class
//            }
//            it.history[1].let { state ->
//                state shouldBeInstanceOf State.Error::class
//                (state as State.Error).exception shouldBeInstanceOf UnknownHostException::class
//            }
//        }
//    }
//
//    @Test
//    fun flowFailedWithValidCache() = runBlockingTest {
//        FailedTestFlowableFactory(dataCache = TestData.ValidData).create().publish().toTest(this).use {
//            delay(100)
//            it.history.size shouldBeEqualTo 1
//            it.history[0].let { state ->
//                state shouldBeInstanceOf State.Completed::class
//                (state as State.Completed).content shouldBeEqualTo TestData.ValidData
//            }
//        }
//    }
//
//    @Test
//    fun flowFailedWithInvalidCache() = runBlockingTest {
//        FailedTestFlowableFactory(dataCache = TestData.InvalidData).create().publish().toTest(this).use {
//            delay(100)
//            it.history.size shouldBeEqualTo 2
//            it.history[0].let { state ->
//                state shouldBeInstanceOf State.Loading::class
//            }
//            it.history[1].let { state ->
//                state shouldBeInstanceOf State.Error::class
//                (state as State.Error).exception shouldBeInstanceOf UnknownHostException::class
//            }
//        }
//    }

    @Test
    fun getFromBothWithNoCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableFactory(dataCache = null).create()
        storeFlowable.requireData(GettingFrom.Both) shouldBeEqualTo TestData.FetchedData
    }

    @Test
    fun getFromBothWithValidCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableFactory(dataCache = TestData.ValidData).create()
        storeFlowable.requireData(GettingFrom.Both) shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun getFromBothWithInvalidCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableFactory(dataCache = TestData.InvalidData).create()
        storeFlowable.requireData(GettingFrom.Both) shouldBeEqualTo TestData.FetchedData
    }

    @Test
    fun getFromCacheWithNoCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableFactory(dataCache = null).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Cache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun getFromCacheWithValidCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableFactory(dataCache = TestData.ValidData).create()
        storeFlowable.requireData(GettingFrom.Cache) shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun getFromCacheWithInvalidCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableFactory(dataCache = TestData.InvalidData).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Cache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun getFromOriginWithNoCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableFactory(dataCache = null).create()
        storeFlowable.requireData(GettingFrom.Origin) shouldBeEqualTo TestData.FetchedData
    }

    @Test
    fun getFromOriginWithValidCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableFactory(dataCache = TestData.ValidData).create()
        storeFlowable.requireData(GettingFrom.Origin) shouldBeEqualTo TestData.FetchedData
    }

    @Test
    fun getFromOriginWithInvalidCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableFactory(dataCache = TestData.InvalidData).create()
        storeFlowable.requireData(GettingFrom.Origin) shouldBeEqualTo TestData.FetchedData
    }

    @Test
    fun getFailedFromBothWithNoCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableFactory(dataCache = null).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Both) } shouldThrow UnknownHostException::class
    }

    @Test
    fun getFailedFromBothWithValidCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableFactory(dataCache = TestData.ValidData).create()
        storeFlowable.requireData(GettingFrom.Both) shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun getFailedFromBothWithInvalidCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableFactory(dataCache = TestData.InvalidData).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Both) } shouldThrow UnknownHostException::class
    }

    @Test
    fun getFailedFromCacheWithNoCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableFactory(dataCache = null).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Cache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun getFailedFromCacheWithValidCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableFactory(dataCache = TestData.ValidData).create()
        storeFlowable.requireData(GettingFrom.Cache) shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun getFailedFromCacheWithInvalidCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableFactory(dataCache = TestData.InvalidData).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Cache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun getFailedFromOriginWithNoCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableFactory(dataCache = null).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Origin) } shouldThrow UnknownHostException::class
    }

    @Test
    fun getFailedFromOriginWithValidCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableFactory(dataCache = TestData.ValidData).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Origin) } shouldThrow UnknownHostException::class
    }

    @Test
    fun getFailedFromOriginWithInvalidCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableFactory(dataCache = TestData.InvalidData).create()
        coInvoking { storeFlowable.requireData(GettingFrom.Origin) } shouldThrow UnknownHostException::class
    }

//    @Test
//    fun updateData() = runBlockingTest {
//        val storeFlowable = SucceedTestFlowableFactory(dataCache = TestData.ValidData).create()
//        storeFlowable.publish().toTest(this).use {
//            storeFlowable.update(TestData.ValidData)
//            it.history.last().let { state ->
//                state shouldBeInstanceOf State.Completed::class
//                (state as State.Completed).content shouldBeEqualTo TestData.ValidData
//            }
//        }
//    }
//
//    @Test
//    fun updateNull() = runBlockingTest {
//        val storeFlowable = SucceedTestFlowableFactory(dataCache = TestData.ValidData).create()
//        storeFlowable.publish().toTest(this).use {
//            storeFlowable.update(null)
//            it.history.last().let { state ->
//                state shouldBeInstanceOf State.Completed::class
//                (state as State.Completed).content shouldBeEqualTo null
//            }
//        }
//    }
//
//    @Test
//    fun validateWithNoCache() = runBlockingTest {
//        val storeFlowable = SucceedTestFlowableFactory(dataCache = TestData.ValidData).create()
//        storeFlowable.publish().toTest(this).use {
//            storeFlowable.update(null)
//            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
//            storeFlowable.validate()
//            it.history.size shouldBeEqualTo 4 // Fixed -> Fixed -> Loading -> Fixed
//        }
//    }
//
//    @Test
//    fun validateWithValidData() = runBlockingTest {
//        val storeFlowable = SucceedTestFlowableFactory(dataCache = TestData.ValidData).create()
//        storeFlowable.publish().toTest(this).use {
//            storeFlowable.update(TestData.ValidData)
//            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
//            storeFlowable.validate()
//            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
//        }
//    }
//
//    @Test
//    fun validateWithInvalidData() = runBlockingTest {
//        val storeFlowable = SucceedTestFlowableFactory(dataCache = TestData.ValidData).create()
//        storeFlowable.publish().toTest(this).use {
//            storeFlowable.update(TestData.InvalidData)
//            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
//            storeFlowable.validate()
//            it.history.size shouldBeEqualTo 4 // Fixed -> Fixed -> Loading -> Fixed
//        }
//    }
//
//    @Test
//    fun refresh() = runBlockingTest {
//        val storeFlowable = SucceedTestFlowableFactory(dataCache = TestData.ValidData).create()
//        storeFlowable.publish().toTest(this).use {
//            it.history.size shouldBeEqualTo 1 // Fixed
//            storeFlowable.refresh()
//            it.history.size shouldBeEqualTo 3 // Fixed -> Loading -> Fixed
//        }
//    }
}
