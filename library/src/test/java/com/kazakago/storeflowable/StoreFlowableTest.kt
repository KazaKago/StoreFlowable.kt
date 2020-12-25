package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.State
import com.kazakago.storeflowable.core.StateContent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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

    private abstract class TestResponder(private var dataCache: TestData?) : StoreFlowableResponder<String, TestData> {

        override val key: String = "Key"

        override val flowableDataStateManager: FlowableDataStateManager<String> = object : FlowableDataStateManager<String>() {}

        override suspend fun loadData(): TestData? {
            return dataCache
        }

        override suspend fun saveData(data: TestData?) {
            dataCache = data
        }

        override suspend fun needRefresh(data: TestData): Boolean {
            return data.needRefresh
        }
    }

    private class SucceedTestResponder(dataCache: TestData?) : TestResponder(dataCache) {

        override suspend fun fetchOrigin(): TestData {
            return TestData.FetchedData
        }
    }

    private class FailedTestResponder(dataCache: TestData?) : TestResponder(dataCache) {

        override suspend fun fetchOrigin(): TestData {
            throw UnknownHostException()
        }
    }

    @Test
    fun flowWithNoCache() = runBlocking {
        SucceedTestResponder(dataCache = null).create().asFlow().toTest(this).use {
            delay(100)
            it.history.size shouldBeEqualTo 2
            it.history[0].let { state ->
                state shouldBeInstanceOf State.Loading::class
                state.content shouldBeInstanceOf StateContent.NotExist::class
            }
            it.history[1].let { state ->
                state shouldBeInstanceOf State.Fixed::class
                state.content shouldBeInstanceOf StateContent.Exist::class
                (state.content as StateContent.Exist).rawContent shouldBeInstanceOf TestData.FetchedData::class
            }
        }
    }

    @Test
    fun flowWithValidCache() = runBlocking {
        SucceedTestResponder(dataCache = TestData.ValidData).create().asFlow().toTest(this).use {
            delay(100)
            it.history.size shouldBeEqualTo 1
            it.history[0].let { state ->
                state shouldBeInstanceOf State.Fixed::class
                state.content shouldBeInstanceOf StateContent.Exist::class
                (state.content as StateContent.Exist).rawContent shouldBeInstanceOf TestData.ValidData::class
            }
        }
    }

    @Test
    fun flowWithInvalidCache() = runBlocking {
        SucceedTestResponder(dataCache = TestData.InvalidData).create().asFlow().toTest(this).use {
            delay(100)
            it.history.size shouldBeEqualTo 2
            it.history[0].let { state ->
                state shouldBeInstanceOf State.Loading::class
                state.content shouldBeInstanceOf StateContent.NotExist::class
            }
            it.history[1].let { state ->
                state shouldBeInstanceOf State.Fixed::class
                state.content shouldBeInstanceOf StateContent.Exist::class
                (state.content as StateContent.Exist).rawContent shouldBeInstanceOf TestData.FetchedData::class
            }
        }
    }

    @Test
    fun flowFailedWithNoCache() = runBlocking {
        FailedTestResponder(dataCache = null).create().asFlow().toTest(this).use {
            delay(100)
            it.history.size shouldBeEqualTo 2
            it.history[0].let { state ->
                state shouldBeInstanceOf State.Loading::class
                state.content shouldBeInstanceOf StateContent.NotExist::class
            }
            it.history[1].let { state ->
                state shouldBeInstanceOf State.Error::class
                (state as State.Error).exception shouldBeInstanceOf UnknownHostException::class
                state.content shouldBeInstanceOf StateContent.NotExist::class
            }
        }
    }

    @Test
    fun flowFailedWithValidCache() = runBlocking {
        FailedTestResponder(dataCache = TestData.ValidData).create().asFlow().toTest(this).use {
            delay(100)
            it.history.size shouldBeEqualTo 1
            it.history[0].let { state ->
                state shouldBeInstanceOf State.Fixed::class
                state.content shouldBeInstanceOf StateContent.Exist::class
                (state.content as StateContent.Exist).rawContent shouldBeInstanceOf TestData.ValidData::class
            }
        }
    }

    @Test
    fun flowFailedWithInvalidCache() = runBlocking {
        FailedTestResponder(dataCache = TestData.InvalidData).create().asFlow().toTest(this).use {
            delay(100)
            it.history.size shouldBeEqualTo 2
            it.history[0].let { state ->
                state shouldBeInstanceOf State.Loading::class
                state.content shouldBeInstanceOf StateContent.NotExist::class
            }
            it.history[1].let { state ->
                state shouldBeInstanceOf State.Error::class
                (state as State.Error).exception shouldBeInstanceOf UnknownHostException::class
                state.content shouldBeInstanceOf StateContent.NotExist::class
            }
        }
    }

    @Test
    fun getFromMixWithNoCache() = runBlockingTest {
        val storeFlowable = SucceedTestResponder(dataCache = null).create()
        storeFlowable.get(AsDataType.Mix) shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun getFromMixWithValidCache() = runBlockingTest {
        val storeFlowable = SucceedTestResponder(dataCache = TestData.ValidData).create()
        storeFlowable.get(AsDataType.Mix) shouldBeInstanceOf TestData.ValidData::class
    }

    @Test
    fun getFromMixWithInvalidCache() = runBlockingTest {
        val storeFlowable = SucceedTestResponder(dataCache = TestData.InvalidData).create()
        storeFlowable.get(AsDataType.Mix) shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun getFromCacheWithNoCache() = runBlockingTest {
        val storeFlowable = SucceedTestResponder(dataCache = null).create()
        coInvoking { storeFlowable.get(AsDataType.FromCache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun getFromCacheWithValidCache() = runBlockingTest {
        val storeFlowable = SucceedTestResponder(dataCache = TestData.ValidData).create()
        storeFlowable.get(AsDataType.FromCache) shouldBeInstanceOf TestData.ValidData::class
    }

    @Test
    fun getFromCacheWithInvalidCache() = runBlockingTest {
        val storeFlowable = SucceedTestResponder(dataCache = TestData.InvalidData).create()
        coInvoking { storeFlowable.get(AsDataType.FromCache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun getFromOriginWithNoCache() = runBlockingTest {
        val storeFlowable = SucceedTestResponder(dataCache = null).create()
        storeFlowable.get(AsDataType.FromOrigin) shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun getFromOriginWithValidCache() = runBlockingTest {
        val storeFlowable = SucceedTestResponder(dataCache = TestData.ValidData).create()
        storeFlowable.get(AsDataType.FromOrigin) shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun getFromOriginWithInvalidCache() = runBlockingTest {
        val storeFlowable = SucceedTestResponder(dataCache = TestData.InvalidData).create()
        storeFlowable.get(AsDataType.FromOrigin) shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun getFailedFromMixWithNoCache() = runBlockingTest {
        val storeFlowable = FailedTestResponder(dataCache = null).create()
        coInvoking { storeFlowable.get(AsDataType.Mix) } shouldThrow UnknownHostException::class
    }

    @Test
    fun getFailedFromMixWithValidCache() = runBlockingTest {
        val storeFlowable = FailedTestResponder(dataCache = TestData.ValidData).create()
        storeFlowable.get(AsDataType.Mix) shouldBeInstanceOf TestData.ValidData::class
    }

    @Test
    fun getFailedFromMixWithInvalidCache() = runBlockingTest {
        val storeFlowable = FailedTestResponder(dataCache = TestData.InvalidData).create()
        coInvoking { storeFlowable.get(AsDataType.Mix) } shouldThrow UnknownHostException::class
    }

    @Test
    fun getFailedFromCacheWithNoCache() = runBlockingTest {
        val storeFlowable = FailedTestResponder(dataCache = null).create()
        coInvoking { storeFlowable.get(AsDataType.FromCache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun getFailedFromCacheWithValidCache() = runBlockingTest {
        val storeFlowable = FailedTestResponder(dataCache = TestData.ValidData).create()
        storeFlowable.get(AsDataType.FromCache) shouldBeInstanceOf TestData.ValidData::class
    }

    @Test
    fun getFailedFromCacheWithInvalidCache() = runBlockingTest {
        val storeFlowable = FailedTestResponder(dataCache = TestData.InvalidData).create()
        coInvoking { storeFlowable.get(AsDataType.FromCache) } shouldThrow NoSuchElementException::class
    }

    @Test
    fun getFailedFromOriginWithNoCache() = runBlockingTest {
        val storeFlowable = FailedTestResponder(dataCache = null).create()
        coInvoking { storeFlowable.get(AsDataType.FromOrigin) } shouldThrow UnknownHostException::class
    }

    @Test
    fun getFailedFromOriginWithValidCache() = runBlockingTest {
        val storeFlowable = FailedTestResponder(dataCache = TestData.ValidData).create()
        coInvoking { storeFlowable.get(AsDataType.FromOrigin) } shouldThrow UnknownHostException::class
    }

    @Test
    fun getFailedFromOriginWithInvalidCache() = runBlockingTest {
        val storeFlowable = FailedTestResponder(dataCache = TestData.InvalidData).create()
        coInvoking { storeFlowable.get(AsDataType.FromOrigin) } shouldThrow UnknownHostException::class
    }

    @Test
    fun updateData() = runBlockingTest {
        val storeFlowable = SucceedTestResponder(dataCache = TestData.ValidData).create()
        storeFlowable.asFlow().toTest(this).use {
            storeFlowable.update(TestData.ValidData)
            it.history.last().let { state ->
                state shouldBeInstanceOf State.Fixed::class
                (state.content as StateContent.Exist).rawContent shouldBeInstanceOf TestData.ValidData::class
            }
        }
    }

    @Test
    fun updateNull() = runBlockingTest {
        val storeFlowable = SucceedTestResponder(dataCache = TestData.ValidData).create()
        storeFlowable.asFlow().toTest(this).use {
            storeFlowable.update(null)
            it.history.last().let { state ->
                state shouldBeInstanceOf State.Fixed::class
                state.content shouldBeInstanceOf StateContent.NotExist::class
            }
        }
    }

    @Test
    fun validateWithNoCache() = runBlockingTest {
        val storeFlowable = SucceedTestResponder(dataCache = TestData.ValidData).create()
        storeFlowable.asFlow().toTest(this).use {
            storeFlowable.update(null)
            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
            storeFlowable.validate()
            it.history.size shouldBeEqualTo 4 // Fixed -> Fixed -> Loading -> Fixed
        }
    }

    @Test
    fun validateWithValidData() = runBlockingTest {
        val storeFlowable = SucceedTestResponder(dataCache = TestData.ValidData).create()
        storeFlowable.asFlow().toTest(this).use {
            storeFlowable.update(TestData.ValidData)
            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
            storeFlowable.validate()
            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
        }
    }

    @Test
    fun validateWithInvalidData() = runBlockingTest {
        val storeFlowable = SucceedTestResponder(dataCache = TestData.ValidData).create()
        storeFlowable.asFlow().toTest(this).use {
            storeFlowable.update(TestData.InvalidData)
            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
            storeFlowable.validate()
            it.history.size shouldBeEqualTo 4 // Fixed -> Fixed -> Loading -> Fixed
        }
    }

    @Test
    fun request() = runBlockingTest {
        val storeFlowable = SucceedTestResponder(dataCache = TestData.ValidData).create()
        storeFlowable.asFlow().toTest(this).use {
            it.history.size shouldBeEqualTo 1 // Fixed
            storeFlowable.request()
            it.history.size shouldBeEqualTo 3 // Fixed -> Loading -> Fixed
        }
    }
}
