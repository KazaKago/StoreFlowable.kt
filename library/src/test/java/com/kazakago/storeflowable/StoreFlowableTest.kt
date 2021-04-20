package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.State
import com.kazakago.storeflowable.core.StateContent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.coInvoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldThrow
import org.junit.Test
import java.net.UnknownHostException

@ExperimentalCoroutinesApi
class StoreFlowableTest {

    private enum class TestData(val needRefresh: Boolean) {
        ValidData(false),
        InvalidData(true),
        FetchedData(false),
    }

    private abstract class TestFlowableCallback(private var dataCache: TestData?) : StoreFlowableCallback<String, TestData> {

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

    private class SucceedTestFlowableCallback(dataCache: TestData?) : TestFlowableCallback(dataCache) {

        override suspend fun fetchDataFromOrigin(): FetchingResult<TestData> {
            return FetchingResult(TestData.FetchedData)
        }
    }

    private class FailedTestFlowableCallback(dataCache: TestData?) : TestFlowableCallback(dataCache) {

        override suspend fun fetchDataFromOrigin(): FetchingResult<TestData> {
            throw UnknownHostException()
        }
    }

// TODO: Fixed `StateFlow` related UnitTest not passing on CI.
//
//    @Test
//    fun flowWithNoCache() = runBlocking {
//        SucceedTestFlowableCallback(dataCache = null).create().publish().toTest(this).use {
//            delay(100)
//            it.history.size shouldBeEqualTo 2
//            it.history[0].let { state ->
//                state shouldBeInstanceOf State.Loading::class
//                state.content shouldBeInstanceOf StateContent.NotExist::class
//            }
//            it.history[1].let { state ->
//                state shouldBeInstanceOf State.Fixed::class
//                state.content shouldBeInstanceOf StateContent.Exist::class
//                (state.content as StateContent.Exist).rawContent shouldBeInstanceOf TestData.FetchedData::class
//            }
//        }
//    }
//
//    @Test
//    fun flowWithValidCache() = runBlocking {
//        SucceedTestFlowableCallback(dataCache = TestData.ValidData).create().publish().toTest(this).use {
//            delay(100)
//            it.history.size shouldBeEqualTo 1
//            it.history[0].let { state ->
//                state shouldBeInstanceOf State.Fixed::class
//                state.content shouldBeInstanceOf StateContent.Exist::class
//                (state.content as StateContent.Exist).rawContent shouldBeInstanceOf TestData.ValidData::class
//            }
//        }
//    }
//
//    @Test
//    fun flowWithInvalidCache() = runBlocking {
//        SucceedTestFlowableCallback(dataCache = TestData.InvalidData).create().publish().toTest(this).use {
//            delay(100)
//            it.history.size shouldBeEqualTo 2
//            it.history[0].let { state ->
//                state shouldBeInstanceOf State.Loading::class
//                state.content shouldBeInstanceOf StateContent.NotExist::class
//            }
//            it.history[1].let { state ->
//                state shouldBeInstanceOf State.Fixed::class
//                state.content shouldBeInstanceOf StateContent.Exist::class
//                (state.content as StateContent.Exist).rawContent shouldBeInstanceOf TestData.FetchedData::class
//            }
//        }
//    }
//
//    @Test
//    fun flowFailedWithNoCache() = runBlocking {
//        FailedTestFlowableCallback(dataCache = null).create().publish().toTest(this).use {
//            delay(100)
//            it.history.size shouldBeEqualTo 2
//            it.history[0].let { state ->
//                state shouldBeInstanceOf State.Loading::class
//                state.content shouldBeInstanceOf StateContent.NotExist::class
//            }
//            it.history[1].let { state ->
//                state shouldBeInstanceOf State.Error::class
//                (state as State.Error).exception shouldBeInstanceOf UnknownHostException::class
//                state.content shouldBeInstanceOf StateContent.NotExist::class
//            }
//        }
//    }
//
//    @Test
//    fun flowFailedWithValidCache() = runBlocking {
//        FailedTestFlowableCallback(dataCache = TestData.ValidData).create().publish().toTest(this).use {
//            delay(100)
//            it.history.size shouldBeEqualTo 1
//            it.history[0].let { state ->
//                state shouldBeInstanceOf State.Fixed::class
//                state.content shouldBeInstanceOf StateContent.Exist::class
//                (state.content as StateContent.Exist).rawContent shouldBeInstanceOf TestData.ValidData::class
//            }
//        }
//    }
//
//    @Test
//    fun flowFailedWithInvalidCache() = runBlocking {
//        FailedTestFlowableCallback(dataCache = TestData.InvalidData).create().publish().toTest(this).use {
//            delay(100)
//            it.history.size shouldBeEqualTo 2
//            it.history[0].let { state ->
//                state shouldBeInstanceOf State.Loading::class
//                state.content shouldBeInstanceOf StateContent.NotExist::class
//            }
//            it.history[1].let { state ->
//                state shouldBeInstanceOf State.Error::class
//                (state as State.Error).exception shouldBeInstanceOf UnknownHostException::class
//                state.content shouldBeInstanceOf StateContent.NotExist::class
//            }
//        }
//    }

    @Test
    fun getFromMixWithNoCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableCallback(dataCache = null).create()
        storeFlowable.requireData(AsDataType.Mix) shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun getFromMixWithValidCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableCallback(dataCache = TestData.ValidData).create()
        storeFlowable.requireData(AsDataType.Mix) shouldBeInstanceOf TestData.ValidData::class
    }

    @Test
    fun getFromMixWithInvalidCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableCallback(dataCache = TestData.InvalidData).create()
        storeFlowable.requireData(AsDataType.Mix) shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun getFromCacheWithNoCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableCallback(dataCache = null).create()
        coInvoking { storeFlowable.requireData(AsDataType.FromCache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun getFromCacheWithValidCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableCallback(dataCache = TestData.ValidData).create()
        storeFlowable.requireData(AsDataType.FromCache) shouldBeInstanceOf TestData.ValidData::class
    }

    @Test
    fun getFromCacheWithInvalidCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableCallback(dataCache = TestData.InvalidData).create()
        coInvoking { storeFlowable.requireData(AsDataType.FromCache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun getFromOriginWithNoCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableCallback(dataCache = null).create()
        storeFlowable.requireData(AsDataType.FromOrigin) shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun getFromOriginWithValidCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableCallback(dataCache = TestData.ValidData).create()
        storeFlowable.requireData(AsDataType.FromOrigin) shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun getFromOriginWithInvalidCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableCallback(dataCache = TestData.InvalidData).create()
        storeFlowable.requireData(AsDataType.FromOrigin) shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun getFailedFromMixWithNoCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableCallback(dataCache = null).create()
        coInvoking { storeFlowable.requireData(AsDataType.Mix) } shouldThrow UnknownHostException::class
    }

    @Test
    fun getFailedFromMixWithValidCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableCallback(dataCache = TestData.ValidData).create()
        storeFlowable.requireData(AsDataType.Mix) shouldBeInstanceOf TestData.ValidData::class
    }

    @Test
    fun getFailedFromMixWithInvalidCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableCallback(dataCache = TestData.InvalidData).create()
        coInvoking { storeFlowable.requireData(AsDataType.Mix) } shouldThrow UnknownHostException::class
    }

    @Test
    fun getFailedFromCacheWithNoCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableCallback(dataCache = null).create()
        coInvoking { storeFlowable.requireData(AsDataType.FromCache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun getFailedFromCacheWithValidCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableCallback(dataCache = TestData.ValidData).create()
        storeFlowable.requireData(AsDataType.FromCache) shouldBeInstanceOf TestData.ValidData::class
    }

    @Test
    fun getFailedFromCacheWithInvalidCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableCallback(dataCache = TestData.InvalidData).create()
        coInvoking { storeFlowable.requireData(AsDataType.FromCache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun getFailedFromOriginWithNoCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableCallback(dataCache = null).create()
        coInvoking { storeFlowable.requireData(AsDataType.FromOrigin) } shouldThrow UnknownHostException::class
    }

    @Test
    fun getFailedFromOriginWithValidCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableCallback(dataCache = TestData.ValidData).create()
        coInvoking { storeFlowable.requireData(AsDataType.FromOrigin) } shouldThrow UnknownHostException::class
    }

    @Test
    fun getFailedFromOriginWithInvalidCache() = runBlockingTest {
        val storeFlowable = FailedTestFlowableCallback(dataCache = TestData.InvalidData).create()
        coInvoking { storeFlowable.requireData(AsDataType.FromOrigin) } shouldThrow UnknownHostException::class
    }

    @Test
    fun updateData() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableCallback(dataCache = TestData.ValidData).create()
        storeFlowable.publish().toTest(this).use {
            storeFlowable.update(TestData.ValidData)
            it.history.last().let { state ->
                state shouldBeInstanceOf State.Fixed::class
                (state.content as StateContent.Exist).rawContent shouldBeInstanceOf TestData.ValidData::class
            }
        }
    }

    @Test
    fun updateNull() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableCallback(dataCache = TestData.ValidData).create()
        storeFlowable.publish().toTest(this).use {
            storeFlowable.update(null)
            it.history.last().let { state ->
                state shouldBeInstanceOf State.Fixed::class
                state.content shouldBeInstanceOf StateContent.NotExist::class
            }
        }
    }

    @Test
    fun validateWithNoCache() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableCallback(dataCache = TestData.ValidData).create()
        storeFlowable.publish().toTest(this).use {
            storeFlowable.update(null)
            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
            storeFlowable.validate()
            it.history.size shouldBeEqualTo 4 // Fixed -> Fixed -> Loading -> Fixed
        }
    }

    @Test
    fun validateWithValidData() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableCallback(dataCache = TestData.ValidData).create()
        storeFlowable.publish().toTest(this).use {
            storeFlowable.update(TestData.ValidData)
            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
            storeFlowable.validate()
            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
        }
    }

    @Test
    fun validateWithInvalidData() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableCallback(dataCache = TestData.ValidData).create()
        storeFlowable.publish().toTest(this).use {
            storeFlowable.update(TestData.InvalidData)
            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
            storeFlowable.validate()
            it.history.size shouldBeEqualTo 4 // Fixed -> Fixed -> Loading -> Fixed
        }
    }

    @Test
    fun refresh() = runBlockingTest {
        val storeFlowable = SucceedTestFlowableCallback(dataCache = TestData.ValidData).create()
        storeFlowable.publish().toTest(this).use {
            it.history.size shouldBeEqualTo 1 // Fixed
            storeFlowable.refresh()
            it.history.size shouldBeEqualTo 3 // Fixed -> Loading -> Fixed
        }
    }
}
