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

@ExperimentalCoroutinesApi
class DataSelectorTwoWayTest {

    private enum class TestData(val needRefresh: Boolean) {
        ValidData(false),
        InvalidData(true),
        FetchedData(false),
        FetchedAppendingData(false),
        FetchedPrependingData(false),
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
                dataCache = newData + (cachedData ?: emptyList())
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
                return InternalFetchingResult(listOf(TestData.FetchedPrependingData), noMoreAppendingData = false, noMorePrependingData = false)
            }
        },
        needRefresh = { it.firstOrNull()?.needRefresh ?: false }
    )

    private var dataState: DataState = DataState.Fixed(mockk(), mockk())
    private var dataCache: List<TestData>? = null

    @Test
    fun requestAppendingAndPrepending_Fixed_Fixed_Fixed_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Fixed_Fixed_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.ValidData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Fixed_Fixed_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.InvalidData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Fixed_FixedWithNoMoreData_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Fixed_FixedWithNoMoreData_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Fixed_FixedWithNoMoreData_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Fixed_Loading_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Loading())
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Fixed_Loading_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Loading())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Fixed_Loading_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Loading())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Fixed_Error_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Error(mockk()))
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Fixed_Error_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Error(mockk()))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.ValidData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Fixed_Error_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Error(mockk()))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.InvalidData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_FixedWithNoMoreData_Fixed_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Fixed())
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_FixedWithNoMoreData_Fixed_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.ValidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_FixedWithNoMoreData_Fixed_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.InvalidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_FixedWithNoMoreData_FixedWithNoMoreData_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_FixedWithNoMoreData_FixedWithNoMoreData_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_FixedWithNoMoreData_FixedWithNoMoreData_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_FixedWithNoMoreData_Loading_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Loading())
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_FixedWithNoMoreData_Loading_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Loading())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_FixedWithNoMoreData_Loading_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Loading())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_FixedWithNoMoreData_Error_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Error(mockk()))
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_FixedWithNoMoreData_Error_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Error(mockk()))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.ValidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_FixedWithNoMoreData_Error_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Error(mockk()))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.InvalidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Loading_Fixed_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Fixed())
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Loading_Fixed_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.ValidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Loading_Fixed_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.InvalidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Loading_FixedWithNoMoreData_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Loading_FixedWithNoMoreData_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Loading_FixedWithNoMoreData_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Loading_Loading_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Loading())
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Loading_Loading_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Loading())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Loading_Loading_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Loading())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Loading_Error_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Error(mockk()))
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Loading_Error_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Error(mockk()))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.ValidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Loading_Error_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Error(mockk()))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.InvalidData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Error_Fixed_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), AdditionalDataState.Fixed())
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Error_Fixed_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.ValidData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Error_Fixed_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.InvalidData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Error_FixedWithNoMoreData_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Error_FixedWithNoMoreData_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Error_FixedWithNoMoreData_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Error_Loading_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), AdditionalDataState.Loading())
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Error_Loading_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), AdditionalDataState.Loading())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Error_Loading_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), AdditionalDataState.Loading())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Error_Error_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), AdditionalDataState.Error(mockk()))
        dataCache = null

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Error_Error_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), AdditionalDataState.Error(mockk()))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.ValidData, TestData.FetchedAppendingData)
    }

    @Test
    fun requestAppendingAndPrepending_Fixed_Error_Error_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(mockk()), AdditionalDataState.Error(mockk()))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestAppendingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData, TestData.FetchedAppendingData)

        dataSelector.requestPrependingData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).appendingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        (dataState as DataState.Fixed).prependingDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.FetchedPrependingData, TestData.InvalidData, TestData.FetchedAppendingData)
    }
}
