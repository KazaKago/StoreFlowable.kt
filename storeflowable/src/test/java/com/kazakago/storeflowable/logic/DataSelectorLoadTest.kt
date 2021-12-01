package com.kazakago.storeflowable.logic

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.DataStateManager
import com.kazakago.storeflowable.origin.InternalFetched
import com.kazakago.storeflowable.origin.OriginDataManager
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Assert.fail
import org.junit.Test

@ExperimentalCoroutinesApi
class DataSelectorLoadTest {

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
                fail()
                throw NotImplementedError()
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
    fun load_NoCache() = runTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = null

        val data = dataSelector.loadValidCacheOrNull()
        data shouldBeEqualTo null
    }

    @Test
    fun load_ValidCache() = runTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = TestData.ValidData

        val data = dataSelector.loadValidCacheOrNull()
        data shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun load_InvalidCache() = runTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = TestData.InvalidData

        val data = dataSelector.loadValidCacheOrNull()
        data shouldBeEqualTo null
    }
}
