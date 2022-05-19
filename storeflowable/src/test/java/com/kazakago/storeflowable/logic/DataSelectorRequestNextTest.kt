package com.kazakago.storeflowable.logic

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.cache.RequestKeyManager
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
class DataSelectorRequestNextTest {

    private enum class TestData(val needRefresh: Boolean) {
        ValidData(false),
        InvalidData(true),
        FetchedData(false),
        FetchedNextData(false),
    }

    private val dataSelector = DataSelector(
        dataStateManager = object : DataStateManager {
            override fun load(): DataState {
                return dataState
            }

            override fun save(state: DataState) {
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
                fail()
            }
        },
        originDataManager = object : OriginDataManager<List<TestData>> {
            override suspend fun fetch(): InternalFetched<List<TestData>> {
                fail()
            }

            override suspend fun fetchNext(nextKey: String): InternalFetched<List<TestData>> {
                return InternalFetched(listOf(TestData.FetchedNextData), nextKey = "NEXT_KEY", prevKey = null)
            }

            override suspend fun fetchPrev(prevKey: String): InternalFetched<List<TestData>> {
                fail()
            }
        },
        requestKeyManager = object : RequestKeyManager {
            override suspend fun loadNext(): String? {
                return nextRequestKey
            }

            override suspend fun saveNext(requestKey: String?) {
                nextRequestKey = requestKey
            }

            override suspend fun loadPrev(): String? {
                fail()
            }

            override suspend fun savePrev(requestKey: String?) {
                // do nothing.
            }
        },
        needRefresh = { it.firstOrNull()?.needRefresh ?: false },
        asyncDispatcher = StandardTestDispatcher(),
    )

    private var dataState: DataState = DataState.Fixed(fakeAdditionalDataState(), fakeAdditionalDataState())
    private var dataCache: List<TestData>? = null
    private var nextRequestKey: String? = null

    @Test
    fun requestNextData_Fixed_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), fakeAdditionalDataState())
        dataCache = null
        nextRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextData_Fixed_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), fakeAdditionalDataState())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData, TestData.FetchedNextData)
        nextRequestKey shouldBe "NEXT_KEY"
    }

    @Test
    fun requestNextData_Fixed_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), fakeAdditionalDataState())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData, TestData.FetchedNextData)
        nextRequestKey shouldBe "NEXT_KEY"
    }

    @Test
    fun requestNextData_Fixed_FixedWithNoMoreData_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), fakeAdditionalDataState())
        dataCache = null
        nextRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe null
        nextRequestKey shouldBe null
    }

    @Test
    fun requestNextData_Fixed_FixedWithNoMoreData_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), fakeAdditionalDataState())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe null
    }

    @Test
    fun requestNextData_Fixed_FixedWithNoMoreData_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), fakeAdditionalDataState())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe null
    }

    @Test
    fun requestNextData_Fixed_Loading_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), fakeAdditionalDataState())
        dataCache = null
        nextRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextData_Fixed_Loading_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), fakeAdditionalDataState())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextData_Fixed_Loading_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), fakeAdditionalDataState())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextData_Fixed_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), fakeAdditionalDataState())
        dataCache = null
        nextRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextData_Fixed_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), fakeAdditionalDataState())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData, TestData.FetchedNextData)
        nextRequestKey shouldBe "NEXT_KEY"
    }

    @Test
    fun requestNextData_Fixed_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), fakeAdditionalDataState())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData, TestData.FetchedNextData)
        nextRequestKey shouldBe "NEXT_KEY"
    }

    @Test
    fun requestNextData_Loading_NoCache() = runTest {
        dataState = DataState.Loading()
        dataCache = null
        nextRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Loading>()
        dataCache shouldBe null
        nextRequestKey shouldBe null
    }

    @Test
    fun requestNextData_Loading_ValidCache() = runTest {
        dataState = DataState.Loading()
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe null
    }

    @Test
    fun requestNextData_Loading_InvalidCache() = runTest {
        dataState = DataState.Loading()
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe null
    }

    @Test
    fun requestNextData_Error_NoCache() = runTest {
        dataState = DataState.Error(fakeException())
        dataCache = null
        nextRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
        nextRequestKey shouldBe null
    }

    @Test
    fun requestNextData_Error_ValidCache() = runTest {
        dataState = DataState.Error(fakeException())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe null
    }

    @Test
    fun requestNextData_Error_InvalidCache() = runTest {
        dataState = DataState.Error(fakeException())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe null
    }

    @Test
    fun requestNextData_NonContinueWhenError_Fixed_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), fakeAdditionalDataState())
        dataCache = null
        nextRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = false)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextData_NonContinueWhenError_Fixed_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), fakeAdditionalDataState())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = false)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextData_NonContinueWhenError_Fixed_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), fakeAdditionalDataState())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = false)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
    }
}
