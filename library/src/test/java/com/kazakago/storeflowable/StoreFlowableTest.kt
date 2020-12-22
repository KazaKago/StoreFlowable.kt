package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.State
import com.kazakago.storeflowable.core.StateContent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Test
import java.lang.Thread.sleep
import java.net.UnknownHostException

@ExperimentalCoroutinesApi
class StoreFlowableTest {

    sealed class TestData(val isStale: Boolean) {
        class CachedData(isStale: Boolean) : TestData(isStale)
        class FetchedData : TestData(false)
        class ManualData(isStale: Boolean = false) : TestData(isStale)
    }

    private data class SucceedTestStoreFlowableResponder(private var dataCache: TestData? = null, override val flowableDataStateManager: FlowableDataStateManager<String> = object : FlowableDataStateManager<String>() {}) : StoreFlowableResponder<String, TestData> {

        override val key: String = "Key"

        override suspend fun loadData(): TestData? {
            return dataCache
        }

        override suspend fun saveData(data: TestData?) {
            dataCache = data
        }

        override suspend fun fetchOrigin(): TestData {
            return TestData.FetchedData()
        }

        override suspend fun needRefresh(data: TestData): Boolean {
            return data.isStale
        }
    }

    private data class FailedTestStoreFlowableResponder(private var dataCache: TestData? = null, override val flowableDataStateManager: FlowableDataStateManager<String> = object : FlowableDataStateManager<String>() {}) : StoreFlowableResponder<String, TestData> {

        override val key: String = "Key"

        override suspend fun loadData(): TestData? {
            return dataCache
        }

        override suspend fun saveData(data: TestData?) {
            dataCache = data
        }

        override suspend fun fetchOrigin(): TestData {
            throw UnknownHostException()
        }

        override suspend fun needRefresh(data: TestData): Boolean {
            return data.isStale
        }
    }

    @Test
    fun validateNotExist() = runBlockingTest {
        SucceedTestStoreFlowableResponder().createStoreFlowable().asFlow().toTest(this).use {
            sleep(100)
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
    fun validateExist() = runBlockingTest {
        SucceedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = false)).createStoreFlowable().asFlow().toTest(this).use {
            sleep(100)
            it.history.size shouldBeEqualTo 1
            it.history[0].let { state ->
                state shouldBeInstanceOf State.Fixed::class
                state.content shouldBeInstanceOf StateContent.Exist::class
                (state.content as StateContent.Exist).rawContent shouldBeInstanceOf TestData.CachedData::class
            }
        }
    }

    @Test
    fun validateStaleExist() = runBlockingTest {
        SucceedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = true)).createStoreFlowable().asFlow().toTest(this).use {
            sleep(100)
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
    fun invalidateNotExist() = runBlockingTest {
        FailedTestStoreFlowableResponder().createStoreFlowable().asFlow().toTest(this).use {
            sleep(100)
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
    fun invalidateExist() = runBlockingTest {
        FailedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = false)).createStoreFlowable().asFlow().toTest(this).use {
            sleep(100)
            it.history.size shouldBeEqualTo 1
            it.history[0].let { state ->
                state shouldBeInstanceOf State.Fixed::class
                state.content shouldBeInstanceOf StateContent.Exist::class
                (state.content as StateContent.Exist).rawContent shouldBeInstanceOf TestData.CachedData::class
            }
        }
    }

    @Test
    fun invalidateStaleExist() = runBlockingTest {
        FailedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = true)).createStoreFlowable().asFlow().toTest(this).use {
            sleep(100)
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
        val storeFlowable = SucceedTestStoreFlowableResponder().createStoreFlowable()
        storeFlowable.get(AsDataType.Mix) shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun getFromMixWithValidateCache() = runBlockingTest {
        val storeFlowable = SucceedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = false)).createStoreFlowable()
        storeFlowable.get(AsDataType.Mix) shouldBeInstanceOf TestData.CachedData::class
    }

    @Test
    fun getFromMixWithInvalidateCache() = runBlockingTest {
        val storeFlowable = SucceedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = true)).createStoreFlowable()
        storeFlowable.get(AsDataType.Mix) shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test(expected = NoSuchElementException::class)
    fun getFromCacheWithNoCache() = runBlockingTest {
        val storeFlowable = SucceedTestStoreFlowableResponder().createStoreFlowable()
        storeFlowable.get(AsDataType.FromCache)
    }

    @Test
    fun getFromCacheWithValidateCache() = runBlockingTest {
        val storeFlowable = SucceedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = false)).createStoreFlowable()
        storeFlowable.get(AsDataType.FromCache) shouldBeInstanceOf TestData.CachedData::class
    }

    @Test(expected = NoSuchElementException::class)
    fun getFromCacheWithInvalidateCache() = runBlockingTest {
        val storeFlowable = SucceedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = true)).createStoreFlowable()
        storeFlowable.get(AsDataType.FromCache)
    }

    @Test
    fun getFromOriginWithNoCache() = runBlockingTest {
        val storeFlowable = SucceedTestStoreFlowableResponder().createStoreFlowable()
        storeFlowable.get(AsDataType.FromOrigin) shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun getFromOriginWithValidateCache() = runBlockingTest {
        val storeFlowable = SucceedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = false)).createStoreFlowable()
        storeFlowable.get(AsDataType.FromOrigin) shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun getFromOriginWithInvalidateCache() = runBlockingTest {
        val storeFlowable = SucceedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = true)).createStoreFlowable()
        storeFlowable.get(AsDataType.FromOrigin) shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test(expected = UnknownHostException::class)
    fun getFailedFromMixWithNoCache() = runBlockingTest {
        val storeFlowable = FailedTestStoreFlowableResponder().createStoreFlowable()
        storeFlowable.get(AsDataType.Mix)
    }

    @Test
    fun getFailedFromMixWithValidateCache() = runBlockingTest {
        val storeFlowable = FailedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = false)).createStoreFlowable()
        storeFlowable.get(AsDataType.Mix) shouldBeInstanceOf TestData.CachedData::class
    }

    @Test(expected = UnknownHostException::class)
    fun getFailedFromMixWithInvalidateCache() = runBlockingTest {
        val storeFlowable = FailedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = true)).createStoreFlowable()
        storeFlowable.get(AsDataType.Mix)
    }

    @Test(expected = NoSuchElementException::class)
    fun getFailedFromCacheWithNoCache() = runBlockingTest {
        val storeFlowable = FailedTestStoreFlowableResponder().createStoreFlowable()
        storeFlowable.get(AsDataType.FromCache)
    }

    @Test
    fun getFailedFromCacheWithValidateCache() = runBlockingTest {
        val storeFlowable = FailedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = false)).createStoreFlowable()
        storeFlowable.get(AsDataType.FromCache) shouldBeInstanceOf TestData.CachedData::class
    }

    @Test(expected = NoSuchElementException::class)
    fun getFailedFromCacheWithInvalidateCache() = runBlockingTest {
        val storeFlowable = FailedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = true)).createStoreFlowable()
        storeFlowable.get(AsDataType.FromCache)
    }

    @Test(expected = UnknownHostException::class)
    fun getFailedFromOriginWithNoCache() = runBlockingTest {
        val storeFlowable = FailedTestStoreFlowableResponder().createStoreFlowable()
        storeFlowable.get(AsDataType.FromOrigin)
    }

    @Test(expected = UnknownHostException::class)
    fun getFailedFromOriginWithValidateCache() = runBlockingTest {
        val storeFlowable = FailedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = false)).createStoreFlowable()
        storeFlowable.get(AsDataType.FromOrigin)
    }

    @Test(expected = UnknownHostException::class)
    fun getFailedFromOriginWithInvalidateCache() = runBlockingTest {
        val storeFlowable = FailedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = true)).createStoreFlowable()
        storeFlowable.get(AsDataType.FromOrigin)
    }

    @Test
    fun updateData() = runBlockingTest {
        val storeFlowable = SucceedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = false)).createStoreFlowable()
        storeFlowable.asFlow().toTest(this).use {
            storeFlowable.update(TestData.ManualData())
            it.history.last().let { state ->
                state shouldBeInstanceOf State.Fixed::class
                (state.content as StateContent.Exist).rawContent shouldBeInstanceOf TestData.ManualData::class
            }
        }
    }

    @Test
    fun updateNull() = runBlockingTest {
        val storeFlowable = SucceedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = false)).createStoreFlowable()
        storeFlowable.asFlow().toTest(this).use {
            storeFlowable.update(null)
            it.history.last().let { state ->
                state shouldBeInstanceOf State.Fixed::class
                state.content shouldBeInstanceOf StateContent.NotExist::class
            }
        }
    }

    @Test
    fun validateWithValidateData() = runBlockingTest {
        val storeFlowable = SucceedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = false)).createStoreFlowable()
        storeFlowable.asFlow().toTest(this).use {
            storeFlowable.update(TestData.ManualData(isStale = false))
            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
            storeFlowable.validate()
            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
        }
    }

    @Test
    fun validateWithInvalidateData() = runBlockingTest {
        val storeFlowable = SucceedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = false)).createStoreFlowable()
        storeFlowable.asFlow().toTest(this).use {
            storeFlowable.update(TestData.ManualData(isStale = true))
            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
            storeFlowable.validate()
            it.history.size shouldBeEqualTo 4 // Fixed -> Fixed -> Loading -> Fixed
        }
    }

    @Test
    fun validateWithNull() = runBlockingTest {
        val storeFlowable = SucceedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = false)).createStoreFlowable()
        storeFlowable.asFlow().toTest(this).use {
            storeFlowable.update(null)
            it.history.size shouldBeEqualTo 2 // Fixed -> Fixed
            storeFlowable.validate()
            it.history.size shouldBeEqualTo 4 // Fixed -> Fixed -> Loading -> Fixed
        }
    }

    @Test
    fun request() = runBlockingTest {
        val storeFlowable = SucceedTestStoreFlowableResponder(dataCache = TestData.CachedData(isStale = false)).createStoreFlowable()
        storeFlowable.asFlow().toTest(this).use {
            it.history.size shouldBeEqualTo 1 // Fixed
            storeFlowable.request()
            it.history.size shouldBeEqualTo 3 // Fixed -> Loading -> Fixed
        }
    }
}
