package com.kazakago.cacheflowable

import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
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
    fun validateFixedNotExist() = runBlocking {
        setupValidateFixedNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))
    }

    private fun setupValidateFixedNotExist() {
        dataState = DataState.Fixed()
        dataCache = null
    }

    @Test
    fun validateLoadingNotExist() = runBlocking {
        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, nullValue())

        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, nullValue())

        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, nullValue())

        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, nullValue())

        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, nullValue())

        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, nullValue())

        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, nullValue())

        setupValidateLoadingNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, nullValue())
    }

    private fun setupValidateLoadingNotExist() {
        dataState = DataState.Loading
        dataCache = null
    }

    @Test
    fun validateErrorNotExist() = runBlocking {
        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Error::class.java)))
        assertThat(dataCache, nullValue())

        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Error::class.java)))
        assertThat(dataCache, nullValue())

        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Error::class.java)))
        assertThat(dataCache, nullValue())

        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Error::class.java)))
        assertThat(dataCache, nullValue())

        setupValidateErrorNotExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))
    }

    private fun setupValidateErrorNotExist() {
        dataState = DataState.Error(mockk())
        dataCache = null
    }

    @Test
    fun validateFixedExist() = runBlocking {
        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))

        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))

        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))

        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))

        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))
    }

    private fun setupValidateFixedExist() {
        dataState = DataState.Fixed()
        dataCache = TestData.CachedData(isStale = false)
    }

    @Test
    fun validateLoadingExist() = runBlocking {
        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))

        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))

        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))

        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))

        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))

        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))

        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))

        setupValidateLoadingExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Loading::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))
    }

    private fun setupValidateLoadingExist() {
        dataState = DataState.Loading
        dataCache = TestData.CachedData(isStale = false)
    }

    @Test
    fun validateErrorExist() = runBlocking {
        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Error::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))

        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Error::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))

        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Error::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))

        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Error::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.CachedData::class.java)))

        setupValidateErrorExist()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))
    }

    private fun setupValidateErrorExist() {
        dataState = DataState.Error(mockk())
        dataCache = TestData.CachedData(isStale = false)
    }

    @Test
    fun validateFixedStaleData() = runBlocking {
        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = false, clearCache = false, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = false)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))

        setupValidateFixedStaleData()
        dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchOnError = true)
        assertThat(dataState, `is`(instanceOf(DataState.Fixed::class.java)))
        assertThat(dataCache, `is`(instanceOf(TestData.FetchedData::class.java)))
    }

    private fun setupValidateFixedStaleData() {
        dataState = DataState.Fixed()
        dataCache = TestData.CachedData(isStale = true)
    }

}
