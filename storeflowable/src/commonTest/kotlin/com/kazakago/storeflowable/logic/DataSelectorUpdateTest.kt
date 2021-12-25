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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.fail

@ExperimentalCoroutinesApi
class DataSelectorUpdateTest {

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
            }

            override suspend fun fetchNext(nextKey: String): InternalFetched<TestData> {
                fail()
            }

            override suspend fun fetchPrev(prevKey: String): InternalFetched<TestData> {
                fail()
            }
        },
        needRefresh = { it.needRefresh },
        asyncDispatcher = StandardTestDispatcher(),
    )

    private var dataState: DataState = DataState.Fixed(fakeAdditionalDataState(), fakeAdditionalDataState())
    private var dataCache: TestData? = null

    @Test
    fun update_Data() = runTest {
        dataState = DataState.Loading()
        dataCache = TestData.ValidData

        dataSelector.update(TestData.FetchedData, null, null)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        dataCache shouldBe TestData.FetchedData
    }

    @Test
    fun update_Null() = runTest {
        dataState = DataState.Error(fakeException())
        dataCache = TestData.InvalidData

        dataSelector.update(null, null, null)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        dataCache shouldBe null
    }
}
