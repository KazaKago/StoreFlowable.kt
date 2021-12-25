package com.kazakago.storeflowable.logic

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.DataStateManager
import com.kazakago.storeflowable.exception.AdditionalRequestOnErrorStateException
import com.kazakago.storeflowable.exception.AdditionalRequestOnNullException
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
class DataSelectorRequestNextAndPrevTest {

    private enum class TestData(val needRefresh: Boolean) {
        ValidData(false),
        InvalidData(true),
        FetchedData(false),
        FetchedNextData(false),
        FetchedPrevData(false),
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
        cacheDataManager = object : CacheDataManager<List<TestData>> {
            override suspend fun load(): List<TestData>? {
                return dataCache
            }

            override suspend fun save(newData: List<TestData>?) {
                fail()
            }

            override suspend fun saveNext(cachedData: List<TestData>, newData: List<TestData>) {
                dataCache = cachedData + newData
            }

            override suspend fun savePrev(cachedData: List<TestData>, newData: List<TestData>) {
                dataCache = newData + cachedData
            }
        },
        originDataManager = object : OriginDataManager<List<TestData>> {
            override suspend fun fetch(): InternalFetched<List<TestData>> {
                fail()
            }

            override suspend fun fetchNext(nextKey: String): InternalFetched<List<TestData>> {
                return InternalFetched(listOf(TestData.FetchedNextData), nextKey = "KEY", prevKey = null)
            }

            override suspend fun fetchPrev(prevKey: String): InternalFetched<List<TestData>> {
                return InternalFetched(listOf(TestData.FetchedPrevData), nextKey = null, prevKey = "KEY")
            }
        },
        needRefresh = { it.firstOrNull()?.needRefresh ?: false },
        asyncDispatcher = StandardTestDispatcher(),
    )

    private var dataState: DataState = DataState.Fixed(fakeAdditionalDataState(), fakeAdditionalDataState())
    private var dataCache: List<TestData>? = null

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Fixed("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.ValidData, TestData.FetchedNextData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.InvalidData, TestData.FetchedNextData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_FixedWithNoMoreData_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_FixedWithNoMoreData_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.ValidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.ValidData, TestData.FetchedNextData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_FixedWithNoMoreData_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.InvalidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.InvalidData, TestData.FetchedNextData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Loading_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Loading("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Loading_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData, TestData.FetchedNextData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Loading_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData, TestData.FetchedNextData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Error("KEY", fakeException()))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Error("KEY", fakeException()))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.ValidData, TestData.FetchedNextData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Error("KEY", fakeException()))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.InvalidData, TestData.FetchedNextData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Fixed("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_FixedWithNoMoreData_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_FixedWithNoMoreData_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_FixedWithNoMoreData_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Loading_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Loading("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Loading_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Loading_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Error("KEY", fakeException()))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Error("KEY", fakeException()))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Error("KEY", fakeException()))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Fixed("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_FixedWithNoMoreData_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_FixedWithNoMoreData_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_FixedWithNoMoreData_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Loading_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Loading("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Loading_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Loading_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Error("KEY", fakeException()))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Error("KEY", fakeException()))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Error("KEY", fakeException()))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", fakeException()), AdditionalDataState.Fixed("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", fakeException()), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.ValidData, TestData.FetchedNextData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", fakeException()), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.InvalidData, TestData.FetchedNextData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_FixedWithNoMoreData_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", fakeException()), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_FixedWithNoMoreData_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", fakeException()), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.ValidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.ValidData, TestData.FetchedNextData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_FixedWithNoMoreData_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", fakeException()), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.InvalidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.FixedWithNoMoreAdditionalData>()
        dataCache shouldBe listOf(TestData.InvalidData, TestData.FetchedNextData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Loading_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", fakeException()), AdditionalDataState.Loading("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Loading_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", fakeException()), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData, TestData.FetchedNextData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Loading_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", fakeException()), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData, TestData.FetchedNextData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", fakeException()), AdditionalDataState.Error("KEY", fakeException()))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", fakeException()), AdditionalDataState.Error("KEY", fakeException()))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.ValidData, TestData.FetchedNextData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", fakeException()), AdditionalDataState.Error("KEY", fakeException()))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData, TestData.FetchedNextData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.FetchedPrevData, TestData.InvalidData, TestData.FetchedNextData)
    }
}
