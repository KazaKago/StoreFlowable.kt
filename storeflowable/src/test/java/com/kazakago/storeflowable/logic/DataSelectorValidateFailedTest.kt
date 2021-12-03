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
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Test
import java.net.UnknownHostException

@ExperimentalCoroutinesApi
class DataSelectorValidateFailedTest {

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
    fun validate_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = null

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf UnknownHostException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun validate_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = TestData.ValidData

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun validate_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = TestData.InvalidData

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf UnknownHostException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun validate_Loading_NoCache() = runTest {
        dataState = DataState.Loading()
        dataCache = null

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun validate_Loading_ValidCache() = runTest {
        dataState = DataState.Loading()
        dataCache = TestData.ValidData

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun validate_Loading_InvalidCache() = runTest {
        dataState = DataState.Loading()
        dataCache = TestData.InvalidData

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo TestData.InvalidData
    }

    @Test
    fun validate_Error_NoCache() = runTest {
        dataState = DataState.Error(mockk())
        dataCache = null

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf UnknownHostException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun validate_Error_ValidCache() = runTest {
        dataState = DataState.Error(mockk())
        dataCache = TestData.ValidData

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf UnknownHostException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun validate_Error_InvalidCache() = runTest {
        dataState = DataState.Error(mockk())
        dataCache = TestData.InvalidData

        dataSelector.validate()
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf UnknownHostException::class
        dataCache shouldBeEqualTo null
    }
}
