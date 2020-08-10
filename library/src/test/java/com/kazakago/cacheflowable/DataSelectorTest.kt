package com.kazakago.cacheflowable

import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.junit.Test

class DataSelectorTest {

    private sealed class TestData(val isStale: Boolean) {
        class CachedData(isStale: Boolean) : TestData(isStale)
        class FetchedData : TestData(false)
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
            override suspend fun load(): TestData? {
                return dataCache
            }

            override suspend fun save(data: TestData?) {
                dataCache = data
            }
        },
        originDataManager = object : OriginDataManager<TestData> {
            override suspend fun fetch(): TestData {
                return TestData.FetchedData()
            }
        },
        needRefresh = { it.isStale }
    )

    private var dataState: DataState = DataState.Fixed()
    private var dataCache: TestData? = null

    @Test
    fun validateFixedNotExist() = runBlockingTest {
        setupValidateFixedNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedNotExist()
        dataSelector.doStateAction(
            forceRefresh = false, clearCache = false, fetchOnError = true
        )
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class
    }

    private fun setupValidateFixedNotExist() {
        dataState = DataState.Fixed()
        dataCache = null
    }

    @Test
    fun validateLoadingNotExist() = runBlockingTest {
        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()

        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()

        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()

        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()

        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()

        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()

        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()

        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache.shouldBeNull()
    }

    private fun setupValidateLoadingNotExist() {
        dataState = DataState.Loading
        dataCache = null
    }

    @Test
    fun validateErrorNotExist() = runBlockingTest {
        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache.shouldBeNull()

        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache.shouldBeNull()

        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache.shouldBeNull()

        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache.shouldBeNull()

        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class
    }

    private fun setupValidateErrorNotExist() {
        dataState = DataState.Error(mockk())
        dataCache = null
    }

    @Test
    fun validateFixedExist() = runBlockingTest {
        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.CachedData::class

        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.CachedData::class

        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.CachedData::class

        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.CachedData::class

        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class
    }

    private fun setupValidateFixedExist() {
        dataState = DataState.Fixed()
        dataCache = TestData.CachedData(isStale = false)
    }

    @Test
    fun validateLoadingExist() = runBlockingTest {
        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.CachedData::class

        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.CachedData::class

        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.CachedData::class

        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.CachedData::class

        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.CachedData::class

        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.CachedData::class

        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.CachedData::class

        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeInstanceOf TestData.CachedData::class
    }

    private fun setupValidateLoadingExist() {
        dataState = DataState.Loading
        dataCache = TestData.CachedData(isStale = false)
    }

    @Test
    fun validateErrorExist() = runBlockingTest {
        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeInstanceOf TestData.CachedData::class

        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeInstanceOf TestData.CachedData::class

        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeInstanceOf TestData.CachedData::class

        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeInstanceOf TestData.CachedData::class

        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class
    }

    private fun setupValidateErrorExist() {
        dataState = DataState.Error(mockk())
        dataCache = TestData.CachedData(isStale = false)
    }

    @Test
    fun validateFixedStaleData() = runBlockingTest {
        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class

        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeInstanceOf TestData.FetchedData::class
    }

    private fun setupValidateFixedStaleData() {
        dataState = DataState.Fixed()
        dataCache = TestData.CachedData(isStale = true)
    }

}
