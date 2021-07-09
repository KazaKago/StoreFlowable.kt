package com.kazakago.storeflowable

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.DataStateManager
import com.kazakago.storeflowable.logic.DataSelector
import com.kazakago.storeflowable.origin.OriginDataManager
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.junit.Test

@ExperimentalCoroutinesApi
class DataSelectorTest {

    private enum class TestData(val needRefresh: Boolean) {
        ValidData(false),
        InvalidData(true),
        FetchedData(false),
    }

    private val dataSelector = DataSelector(
        key = "key",
        dataStateManager = object : DataStateManager<String> {
            override fun load(key: String): DataState {
                return dataState
            }

            override fun save(key: String, state: DataState) {
                dataState = state
            }
        },
        cacheDataManager = object : CacheDataManager<TestData> {
            override suspend fun loadData(): TestData? {
                return dataCache
            }

            override suspend fun saveData(newData: TestData?) {
                dataCache = newData
            }
        },
        originDataManager = object : OriginDataManager<TestData> {
            override suspend fun fetchData(): FetchingResult<TestData> {
                return FetchingResult(TestData.FetchedData)
            }
        },
        needRefresh = { it.needRefresh }
    )

    private var dataState: DataState = DataState.Fixed()
    private var dataCache: TestData? = null

    private fun setupFixedStatNoCache() {
        dataState = DataState.Fixed()
        dataCache = null
    }

    private fun setupLoadingStateNoCache() {
        dataState = DataState.Loading()
        dataCache = null
    }

    private fun setupErrorStateNoCache() {
        dataState = DataState.Error(mockk())
        dataCache = null
    }

    private fun setupFixedStateValidCache() {
        dataState = DataState.Fixed()
        dataCache = TestData.ValidData
    }

    private fun setupLoadingStateValidCache() {
        dataState = DataState.Loading()
        dataCache = TestData.ValidData
    }

    private fun setupErrorStateValidCache() {
        dataState = DataState.Error(mockk())
        dataCache = TestData.ValidData
    }

    private fun setupFixedStateInvalidData() {
        dataState = DataState.Fixed()
        dataCache = TestData.InvalidData
    }

    @Test
    fun doActionWithFixedStateNoCache() = runBlockingTest {
        setupFixedStatNoCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStatNoCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStatNoCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStatNoCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStatNoCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStatNoCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStatNoCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStatNoCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun doActionWithLoadingStateNoCache() = runBlockingTest {
        setupLoadingStateNoCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()

        setupLoadingStateNoCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()

        setupLoadingStateNoCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()

        setupLoadingStateNoCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()

        setupLoadingStateNoCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()

        setupLoadingStateNoCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()

        setupLoadingStateNoCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()

        setupLoadingStateNoCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()
    }

    @Test
    fun doActionWithErrorStateNoCache() = runBlockingTest {
        setupErrorStateNoCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache.shouldBeNull()

        setupErrorStateNoCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupErrorStateNoCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache.shouldBeNull()

        setupErrorStateNoCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupErrorStateNoCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache.shouldBeNull()

        setupErrorStateNoCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupErrorStateNoCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache.shouldBeNull()

        setupErrorStateNoCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun doActionWithFixedStateValidCache() = runBlockingTest {
        setupFixedStateValidCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupFixedStateValidCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupFixedStateValidCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupFixedStateValidCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupFixedStateValidCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStateValidCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStateValidCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStateValidCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun doActionWithLoadingStateValidCache() = runBlockingTest {
        setupLoadingStateValidCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupLoadingStateValidCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupLoadingStateValidCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupLoadingStateValidCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupLoadingStateValidCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupLoadingStateValidCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupLoadingStateValidCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupLoadingStateValidCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.ValidData::class
    }

    @Test
    fun doActionWithErrorStateValidCache() = runBlockingTest {
        setupErrorStateValidCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupErrorStateValidCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupErrorStateValidCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupErrorStateValidCache()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupErrorStateValidCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupErrorStateValidCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupErrorStateValidCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeInstanceOf TestData.ValidData::class

        setupErrorStateValidCache()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class
    }

    @Test
    fun doActionWithFixedStateInvalidData() = runBlockingTest {
        setupFixedStateInvalidData()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStateInvalidData()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStateInvalidData()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStateInvalidData()
        dataSelector.doStateAction(forceRefresh = false, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStateInvalidData()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStateInvalidData()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStateInvalidData()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = false, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupFixedStateInvalidData()
        dataSelector.doStateAction(forceRefresh = true, clearCacheBeforeFetching = true, clearCacheWhenFetchFails = true, continueWhenError = true, awaitFetching = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class
    }
}
