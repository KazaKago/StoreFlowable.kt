package com.kazakago.storeflowable

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.DataStateManager
import com.kazakago.storeflowable.logic.DataSelector
import com.kazakago.storeflowable.logic.RequestType
import com.kazakago.storeflowable.origin.InternalFetchingResult
import com.kazakago.storeflowable.origin.OriginDataManager
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Test

@ExperimentalCoroutinesApi
class DataSelectorPaginatingTest {

    companion object {
        private const val FORCE_REFRESH = false
        private const val CLEAR_CACHE_BEFORE_FETCHING = false // No effect this tests.
        private const val CLEAR_CACHE_WHEN_FETCH_FAILS = false // No effect this tests.
        private const val CONTINUE_WHEN_ERROR = true
        private const val AWAIT_FETCHING = true
        private val REQUEST_TYPE = RequestType.Append
    }

    private enum class TestData(val needRefresh: Boolean) {
        ValidData(false),
        InvalidData(true),
        FetchedData(false),
        FetchedAppendingData(false),
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
        cacheDataManager = object : CacheDataManager<List<TestData>> {
            override suspend fun load(): List<TestData>? {
                return dataCache
            }

            override suspend fun save(newData: List<TestData>?) {
                fail()
            }

            override suspend fun saveAppending(cachedData: List<TestData>?, newData: List<TestData>) {
                dataCache = (cachedData ?: emptyList()) + newData
            }

            override suspend fun savePrepending(cachedData: List<TestData>?, newData: List<TestData>) {
                fail()
            }
        },
        originDataManager = object : OriginDataManager<List<TestData>> {
            override suspend fun fetch(): InternalFetchingResult<List<TestData>> {
                fail()
                throw NotImplementedError()
            }

            override suspend fun fetchAppending(cachedData: List<TestData>?): InternalFetchingResult<List<TestData>> {
                return InternalFetchingResult(listOf(TestData.FetchedAppendingData), noMoreAppendingData = false, noMorePrependingData = false)
            }

            override suspend fun fetchPrepending(cachedData: List<TestData>?): InternalFetchingResult<List<TestData>> {
                fail()
                throw NotImplementedError()
            }
        },
        needRefresh = { it.firstOrNull()?.needRefresh ?: false }
    )

    private var dataState: DataState = DataState.Fixed(appendingDataState = AdditionalDataState.Fixed(), prependingDataState = AdditionalDataState.Fixed())
    private var dataCache: List<TestData>? = null

    @Test
    fun doStateAction_Fixed_Fixed_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), mockk())
        dataCache = emptyList()
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedAppendingData)
    }

    @Test
    fun doStateAction_Fixed_Fixed_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), mockk())
        dataCache = listOf(TestData.ValidData)
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData, TestData.FetchedAppendingData)
    }

    @Test
    fun doStateAction_Fixed_Fixed_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), mockk())
        dataCache = listOf(TestData.InvalidData)
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData, TestData.FetchedAppendingData)
    }

    @Test
    fun doStateAction_Fixed_FixedWithNoMoreData_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), mockk())
        dataCache = null
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Fixed_FixedWithNoMoreData_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), mockk())
        dataCache = listOf(TestData.ValidData)
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun doStateAction_Fixed_FixedWithNoMoreData_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), mockk())
        dataCache = listOf(TestData.InvalidData)
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun doStateAction_Fixed_Loading_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), mockk())
        dataCache = null
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Fixed_Loading_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), mockk())
        dataCache = listOf(TestData.ValidData)
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun doStateAction_Fixed_Loading_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), mockk())
        dataCache = listOf(TestData.InvalidData)
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun doStateAction_Fixed_Error_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), mockk())
        dataCache = null
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedAppendingData)
    }

    @Test
    fun doStateAction_Fixed_Error_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), mockk())
        dataCache = listOf(TestData.ValidData)
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData, TestData.FetchedAppendingData)
    }

    @Test
    fun doStateAction_Fixed_Error_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), mockk())
        dataCache = listOf(TestData.InvalidData)
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData, TestData.FetchedAppendingData)
    }

    @Test
    fun doStateAction_Loading_NoCache() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = null
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Loading_ValidCache() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = listOf(TestData.ValidData)
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun doStateAction_Loading_InvalidCache() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = listOf(TestData.InvalidData)
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun doStateAction_Error_NoCache() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = null
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedAppendingData)
    }

    @Test
    fun doStateAction_Error_ValidCache() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = listOf(TestData.ValidData)
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData, TestData.FetchedAppendingData)
    }

    @Test
    fun doStateAction_Error_InvalidCache() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = listOf(TestData.InvalidData)
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData, TestData.FetchedAppendingData)
    }

    @Test
    fun doStateAction_Error_NoCache_NonContinueWhenError() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = null
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, continueWhenError = false, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Error_ValidCache_NonContinueWhenError() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = listOf(TestData.ValidData)
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, continueWhenError = false, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun doStateAction_Error_InvalidCache_NonContinueWhenError() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = listOf(TestData.InvalidData)
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, continueWhenError = false, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }
}
