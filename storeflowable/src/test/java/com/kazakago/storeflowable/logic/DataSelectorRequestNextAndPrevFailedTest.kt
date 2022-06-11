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
class DataSelectorRequestNextAndPrevFailedTest {

    private enum class TestData(val needRefresh: Boolean) {
        ValidData(false),
        InvalidData(true),
        FetchedData(false),
        FetchedNextData(false),
        FetchedPrevData(false),
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
                dataCache = newData + cachedData
            }
        },
        originDataManager = object : OriginDataManager<List<TestData>> {
            override suspend fun fetch(): InternalFetched<List<TestData>> {
                fail()
            }

            override suspend fun fetchNext(nextKey: String): InternalFetched<List<TestData>> {
                throw NoSuchElementException()
            }

            override suspend fun fetchPrev(prevKey: String): InternalFetched<List<TestData>> {
                throw NoSuchElementException()
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
                return prevRequestKey
            }

            override suspend fun savePrev(requestKey: String?) {
                prevRequestKey = requestKey
            }
        },
        needRefresh = { it.firstOrNull()?.needRefresh ?: false },
        asyncDispatcher = StandardTestDispatcher(),
    )

    private var dataState: DataState = DataState.Fixed(fakeAdditionalDataState(), fakeAdditionalDataState())
    private var dataCache: List<TestData>? = null
    private var nextRequestKey: String? = null
    private var prevRequestKey: String? = null

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())
        dataCache = null
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_FixedWithNoMoreData_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())
        dataCache = null
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_FixedWithNoMoreData_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_FixedWithNoMoreData_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Loading_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Loading())
        dataCache = null
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Loading_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Loading())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Loading_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Loading())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Error(fakeException()))
        dataCache = null
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Error(fakeException()))
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Error(fakeException()))
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())
        dataCache = null
        nextRequestKey = null
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe null
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = null
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = null
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_FixedWithNoMoreData_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())
        dataCache = null
        nextRequestKey = null
        prevRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe null
        nextRequestKey shouldBe null
        prevRequestKey shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe null
        nextRequestKey shouldBe null
        prevRequestKey shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_FixedWithNoMoreData_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = null
        prevRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_FixedWithNoMoreData_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = null
        prevRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Loading_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Loading())
        dataCache = null
        nextRequestKey = null
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe null
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe null
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Loading_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Loading())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = null
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Loading_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Loading())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = null
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Error(fakeException()))
        dataCache = null
        nextRequestKey = null
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe null
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Error(fakeException()))
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = null
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Error(fakeException()))
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = null
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe null
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Fixed())
        dataCache = null
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_FixedWithNoMoreData_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Fixed())
        dataCache = null
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_FixedWithNoMoreData_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_FixedWithNoMoreData_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Loading_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Loading())
        dataCache = null
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Loading_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Loading())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Loading_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Loading())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Error(fakeException()))
        dataCache = null
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Error(fakeException()))
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading(), AdditionalDataState.Error(fakeException()))
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), AdditionalDataState.Fixed())
        dataCache = null
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_FixedWithNoMoreData_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), AdditionalDataState.Fixed())
        dataCache = null
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_FixedWithNoMoreData_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_FixedWithNoMoreData_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), AdditionalDataState.Fixed())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Fixed>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe null
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Loading_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), AdditionalDataState.Loading())
        dataCache = null
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Loading_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), AdditionalDataState.Loading())
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Loading_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), AdditionalDataState.Loading())
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Loading>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), AdditionalDataState.Error(fakeException()))
        dataCache = null
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnNullException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Error>()
        (dataState as DataState.Error).exception.shouldBeTypeOf<AdditionalRequestOnErrorStateException>()
        dataCache shouldBe null
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), AdditionalDataState.Error(fakeException()))
        dataCache = listOf(TestData.ValidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.ValidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error(fakeException()), AdditionalDataState.Error(fakeException()))
        dataCache = listOf(TestData.InvalidData)
        nextRequestKey = "INITIAL_KEY"
        prevRequestKey = "INITIAL_KEY"

        dataSelector.requestNextData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"

        dataSelector.requestPrevData(continueWhenError = true)
        dataState.shouldBeTypeOf<DataState.Fixed>()
        (dataState as DataState.Fixed).nextDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        (dataState as DataState.Fixed).prevDataState.shouldBeTypeOf<AdditionalDataState.Error>()
        dataCache shouldBe listOf(TestData.InvalidData)
        nextRequestKey shouldBe "INITIAL_KEY"
        prevRequestKey shouldBe "INITIAL_KEY"
    }
}
