package com.kazakago.storeflowable.logic

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.DataStateManager
import com.kazakago.storeflowable.exception.AdditionalRequestOnErrorStateException
import com.kazakago.storeflowable.exception.AdditionalRequestOnNullException
import com.kazakago.storeflowable.origin.InternalFetched
import com.kazakago.storeflowable.origin.OriginDataManager
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import java.net.UnknownHostException
import kotlin.test.Test
import kotlin.test.fail

@ExperimentalCoroutinesApi
class DataSelectorRequestNextFailedTest {

    private enum class TestData(val needRefresh: Boolean) {
        ValidData(false),
        InvalidData(true),
        FetchedData(false),
        FetchedNextData(false),
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
                fail()
            }
        },
        originDataManager = object : OriginDataManager<List<TestData>> {
            override suspend fun fetch(): InternalFetched<List<TestData>> {
                fail()
            }

            override suspend fun fetchNext(nextKey: String): InternalFetched<List<TestData>> {
                throw UnknownHostException()
            }

            override suspend fun fetchPrev(prevKey: String): InternalFetched<List<TestData>> {
                fail()
            }
        },
        needRefresh = { it.firstOrNull()?.needRefresh ?: false }
    )

    private var dataState: DataState = DataState.Fixed(mockk(), mockk())
    private var dataCache: List<TestData>? = null

    @Test
    fun requestNextData_Fixed_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), mockk())
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnNullException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextData_Fixed_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), mockk())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextData_Fixed_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), mockk())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextData_Fixed_FixedWithNoMoreData_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), mockk())
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextData_Fixed_FixedWithNoMoreData_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), mockk())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextData_Fixed_FixedWithNoMoreData_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), mockk())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextData_Fixed_Loading_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), mockk())
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextData_Fixed_Loading_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), mockk())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextData_Fixed_Loading_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), mockk())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextData_Fixed_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), mockk())
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnNullException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextData_Fixed_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), mockk())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextData_Fixed_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), mockk())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextData_Loading_NoCache() = runTest {
        dataState = DataState.Loading()
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextData_Loading_ValidCache() = runTest {
        dataState = DataState.Loading()
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextData_Loading_InvalidCache() = runTest {
        dataState = DataState.Loading()
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextData_Error_NoCache() = runTest {
        dataState = DataState.Error(mockk())
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnErrorStateException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextData_Error_ValidCache() = runTest {
        dataState = DataState.Error(mockk())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnErrorStateException::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextData_Error_InvalidCache() = runTest {
        dataState = DataState.Error(mockk())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnErrorStateException::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextData_NonContinueWhenError_Fixed_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), mockk())
        dataCache = null

        dataSelector.requestNextData(continueWhenError = false)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextData_NonContinueWhenError_Fixed_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), mockk())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = false)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextData_NonContinueWhenError_Fixed_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), mockk())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = false)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }
}
