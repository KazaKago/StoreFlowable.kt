package com.kazakago.storeflowable.logic

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.DataStateManager
import com.kazakago.storeflowable.fakeAdditionalDataState
import com.kazakago.storeflowable.fakeException
import com.kazakago.storeflowable.origin.InternalFetched
import com.kazakago.storeflowable.origin.OriginDataManager
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.fail

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
                throw NoSuchElementException()
            }

            override suspend fun fetchNext(nextKey: String): InternalFetched<TestData> {
                fail()
            }

            override suspend fun fetchPrev(prevKey: String): InternalFetched<TestData> {
                fail()
            }
        },
        needRefresh = { it.needRefresh }
    )

    private var dataState: DataState = DataState.Fixed(fakeAdditionalDataState(), fakeAdditionalDataState())
    private var dataCache: TestData? = null

    @Test
    fun validate_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(fakeAdditionalDataState(), fakeAdditionalDataState())
        dataCache = null

        dataSelector.validate()
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<NoSuchElementException>()
        dataCache shouldBe null
    }

    @Test
    fun validate_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(fakeAdditionalDataState(), fakeAdditionalDataState())
        dataCache = TestData.ValidData

        dataSelector.validate()
        dataState.shouldBeTypeOf<DataState.Fixed>()
        dataCache shouldBe TestData.ValidData
    }

    @Test
    fun validate_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(fakeAdditionalDataState(), fakeAdditionalDataState())
        dataCache = TestData.InvalidData

        dataSelector.validate()
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<NoSuchElementException>()
        dataCache shouldBe null
    }

    @Test
    fun validate_Loading_NoCache() = runTest {
        dataState = DataState.Loading()
        dataCache = null

        dataSelector.validate()
        dataState.shouldBeTypeOf<DataState.Loading>()
        dataCache shouldBe null
    }

    @Test
    fun validate_Loading_ValidCache() = runTest {
        dataState = DataState.Loading()
        dataCache = TestData.ValidData

        dataSelector.validate()
        dataState.shouldBeTypeOf<DataState.Loading>()
        dataCache shouldBe TestData.ValidData
    }

    @Test
    fun validate_Loading_InvalidCache() = runTest {
        dataState = DataState.Loading()
        dataCache = TestData.InvalidData

        dataSelector.validate()
        dataState.shouldBeTypeOf<DataState.Loading>()
        dataCache shouldBe TestData.InvalidData
    }

    @Test
    fun validate_Error_NoCache() = runTest {
        dataState = DataState.Error(fakeException())
        dataCache = null

        dataSelector.validate()
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<NoSuchElementException>()
        dataCache shouldBe null
    }

    @Test
    fun validate_Error_ValidCache() = runTest {
        dataState = DataState.Error(fakeException())
        dataCache = TestData.ValidData

        dataSelector.validate()
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<NoSuchElementException>()
        dataCache shouldBe null
    }

    @Test
    fun validate_Error_InvalidCache() = runTest {
        dataState = DataState.Error(fakeException())
        dataCache = TestData.InvalidData

        dataSelector.validate()
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<NoSuchElementException>()
        dataCache shouldBe null
    }
}
