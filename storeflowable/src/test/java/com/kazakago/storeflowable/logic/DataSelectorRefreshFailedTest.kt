package com.kazakago.storeflowable.logic

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.DataStateManager
import com.kazakago.storeflowable.origin.InternalFetched
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
class DataSelectorRefreshFailedTest {

    private enum class TestData(val needRefresh: Boolean) {
        ValidData(false),
        InvalidData(true),
        FetchedData(false),
    }

    private val dataSelector = DataSelector(
        param = Unit,
        dataStateManager = object : DataStateManager<Unit> {
            override fun load(param: Unit): DataState {
                return dataState
            }

            override fun save(param: Unit, state: DataState) {
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

            override suspend fun saveNext(cachedData: TestData, newData: TestData) {
                fail()
            }

            override suspend fun savePrev(cachedData: TestData, newData: TestData) {
                fail()
            }
        },
        originDataManager = object : OriginDataManager<TestData> {
            override suspend fun fetch(): InternalFetched<TestData> {
                throw UnknownHostException()
            }

            override suspend fun fetchNext(nextKey: String): InternalFetched<TestData> {
                fail()
                throw NotImplementedError()
            }

            override suspend fun fetchPrev(prevKey: String): InternalFetched<TestData> {
                fail()
                throw NotImplementedError()
            }
        },
        needRefresh = { it.needRefresh }
    )

    private var dataState: DataState = DataState.Fixed(mockk(), mockk())
    private var dataCache: TestData? = null

    @Test
    fun refresh_Fixed_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = null

        dataSelector.refresh(clearCacheBeforeFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf UnknownHostException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun refresh_Fixed_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = TestData.ValidData

        dataSelector.refresh(clearCacheBeforeFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf UnknownHostException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun refresh_Fixed_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = TestData.InvalidData

        dataSelector.refresh(clearCacheBeforeFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf UnknownHostException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun refresh_Loading_NoCache() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = null

        dataSelector.refresh(clearCacheBeforeFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun refresh_Loading_ValidCache() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = TestData.ValidData

        dataSelector.refresh(clearCacheBeforeFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun refresh_Loading_InvalidCache() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = TestData.InvalidData

        dataSelector.refresh(clearCacheBeforeFetching = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo TestData.InvalidData
    }

    @Test
    fun refresh_Error_NoCache() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = null

        dataSelector.refresh(clearCacheBeforeFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf UnknownHostException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun refresh_Error_ValidCache() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = TestData.ValidData

        dataSelector.refresh(clearCacheBeforeFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf UnknownHostException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun refresh_Error_InvalidCache() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = TestData.InvalidData

        dataSelector.refresh(clearCacheBeforeFetching = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf UnknownHostException::class
        dataCache shouldBeEqualTo null
    }
}
