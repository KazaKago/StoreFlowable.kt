package com.kazakago.storeflowable.logic

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.DataStateManager
import com.kazakago.storeflowable.origin.InternalFetchingResult
import com.kazakago.storeflowable.origin.OriginDataManager
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Test
import java.net.UnknownHostException

@ExperimentalCoroutinesApi
class DataSelectorOneWayFailedTest {

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
                throw UnknownHostException()
            }

            override suspend fun fetchPrepending(cachedData: List<TestData>?): InternalFetchingResult<List<TestData>> {
                fail()
                throw NotImplementedError()
            }
        },
        needRefresh = { it.firstOrNull()?.needRefresh ?: false }
    )

    private var dataState: DataState = DataState.Fixed(mockk(), mockk())
    private var dataCache: List<TestData>? = null

    @Test
    fun requestAppendingData_Fixed_Fixed_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), mockk())
        dataCache = null
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestAppendingData_Fixed_Fixed_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), mockk())
        dataCache = listOf(TestData.ValidData)
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestAppendingData_Fixed_Fixed_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), mockk())
        dataCache = listOf(TestData.InvalidData)
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestAppendingData_Fixed_FixedWithNoMoreData_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), mockk())
        dataCache = null
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestAppendingData_Fixed_FixedWithNoMoreData_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), mockk())
        dataCache = listOf(TestData.ValidData)
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestAppendingData_Fixed_FixedWithNoMoreData_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), mockk())
        dataCache = listOf(TestData.InvalidData)
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestAppendingData_Fixed_Loading_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), mockk())
        dataCache = null
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestAppendingData_Fixed_Loading_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), mockk())
        dataCache = listOf(TestData.ValidData)
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestAppendingData_Fixed_Loading_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), mockk())
        dataCache = listOf(TestData.InvalidData)
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestAppendingData_Fixed_Error_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), mockk())
        dataCache = null
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestAppendingData_Fixed_Error_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), mockk())
        dataCache = listOf(TestData.ValidData)
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestAppendingData_Fixed_Error_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), mockk())
        dataCache = listOf(TestData.InvalidData)
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestAppendingData_Loading_NoCache() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = null
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestAppendingData_Loading_ValidCache() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = listOf(TestData.ValidData)
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestAppendingData_Loading_InvalidCache() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = listOf(TestData.InvalidData)
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestAppendingData_Error_NoCache() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = null
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestAppendingData_Error_ValidCache() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = listOf(TestData.ValidData)
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestAppendingData_Error_InvalidCache() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = listOf(TestData.InvalidData)
        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestAppendingData_Error_NoCache_NonContinueWhenError() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = null
        dataSelector.requestAppendingData(continueWhenError = false)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestAppendingData_Error_ValidCache_NonContinueWhenError() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = listOf(TestData.ValidData)
        dataSelector.requestAppendingData(continueWhenError = false)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestAppendingData_Error_InvalidCache_NonContinueWhenError() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = listOf(TestData.InvalidData)
        dataSelector.requestAppendingData(continueWhenError = false)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }
}
