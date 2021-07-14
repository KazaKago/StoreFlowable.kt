package com.kazakago.storeflowable.logic

import com.kazakago.storeflowable.cache.CacheDataManager
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
class DataSelectorValidateTest {

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
            override suspend fun load(): TestData? {
                return dataCache
            }

            override suspend fun save(newData: TestData?) {
                dataCache = newData
            }

            override suspend fun saveNext(cachedData: TestData?, newData: TestData) {
                fail()
            }

            override suspend fun savePrev(cachedData: TestData?, newData: TestData) {
                fail()
            }
        },
        originDataManager = object : OriginDataManager<TestData> {
            override suspend fun fetch(): InternalFetchingResult<TestData> {
                return InternalFetchingResult(TestData.FetchedData, nextKey = null, prevKey = null)
            }

            override suspend fun fetchNext(nextKey: String): InternalFetchingResult<TestData> {
                fail()
                throw NotImplementedError()
            }

            override suspend fun fetchPrev(prevKey: String): InternalFetchingResult<TestData> {
                fail()
                throw NotImplementedError()
            }
        },
        needRefresh = { it.needRefresh }
    )

    private var dataState: DataState = DataState.Fixed(mockk(), mockk())
    private var dataCache: TestData? = null

    @Test
    fun validate_Fixed_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = null

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeEqualTo TestData.FetchedData
    }

    @Test
    fun validate_Fixed_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = TestData.ValidData

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun validate_Fixed_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = TestData.InvalidData

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeEqualTo TestData.FetchedData
    }

    @Test
    fun validate_Loading_NoCache() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = null

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun validate_Loading_ValidCache() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = TestData.ValidData

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun validate_Loading_InvalidCache() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = TestData.InvalidData

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo TestData.InvalidData
    }

    @Test
    fun validate_Error_NoCache() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = null

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeEqualTo TestData.FetchedData
    }

    @Test
    fun validate_Error_ValidCache() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = TestData.ValidData

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeEqualTo TestData.FetchedData
    }

    @Test
    fun validate_Error_InvalidCache() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = TestData.InvalidData

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeEqualTo TestData.FetchedData
    }
}