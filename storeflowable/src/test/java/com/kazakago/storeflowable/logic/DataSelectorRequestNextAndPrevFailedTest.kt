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
import org.junit.Assert.fail
import org.junit.Test
import java.net.UnknownHostException

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
                throw NotImplementedError()
            }

            override suspend fun fetchNext(nextKey: String): InternalFetched<List<TestData>> {
                throw UnknownHostException()
            }

            override suspend fun fetchPrev(prevKey: String): InternalFetched<List<TestData>> {
                throw UnknownHostException()
            }
        },
        needRefresh = { it.firstOrNull()?.needRefresh ?: false }
    )

    private var dataState: DataState = DataState.Fixed(mockk(), mockk())
    private var dataCache: List<TestData>? = null

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Fixed("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnNullException::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnErrorStateException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_FixedWithNoMoreData_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnNullException::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnErrorStateException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_FixedWithNoMoreData_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_FixedWithNoMoreData_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Loading_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Loading("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnNullException::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnErrorStateException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Loading_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Loading_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Error("KEY", mockk()))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnNullException::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnErrorStateException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Error("KEY", mockk()))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Fixed_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Fixed("KEY"), AdditionalDataState.Error("KEY", mockk()))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Fixed("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnNullException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_FixedWithNoMoreData_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_FixedWithNoMoreData_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_FixedWithNoMoreData_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Loading_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Loading("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Loading_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Loading_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Error("KEY", mockk()))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnNullException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Error("KEY", mockk()))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_FixedWithNoMoreData_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.FixedWithNoMoreAdditionalData(), AdditionalDataState.Error("KEY", mockk()))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Fixed("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnNullException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_FixedWithNoMoreData_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_FixedWithNoMoreData_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_FixedWithNoMoreData_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Loading_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Loading("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Loading_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Loading_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Error("KEY", mockk()))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnNullException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Error("KEY", mockk()))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Loading_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Loading("KEY"), AdditionalDataState.Error("KEY", mockk()))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Fixed_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), AdditionalDataState.Fixed("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnNullException::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnErrorStateException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Fixed_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Fixed_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), AdditionalDataState.Fixed("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Fixed::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_FixedWithNoMoreData_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnNullException::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnErrorStateException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_FixedWithNoMoreData_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_FixedWithNoMoreData_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), AdditionalDataState.FixedWithNoMoreAdditionalData())
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.FixedWithNoMoreAdditionalData::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Loading_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), AdditionalDataState.Loading("KEY"))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnNullException::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnErrorStateException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Loading_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Loading_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), AdditionalDataState.Loading("KEY"))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Loading::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Error_NoCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), AdditionalDataState.Error("KEY", mockk()))
        dataCache = null

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnNullException::class
        dataCache shouldBeEqualTo null

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Error::class
        (dataState as DataState.Error).exception shouldBeInstanceOf AdditionalRequestOnErrorStateException::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Error_ValidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), AdditionalDataState.Error("KEY", mockk()))
        dataCache = listOf(TestData.ValidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.ValidData)
    }

    @Test
    fun requestNextAndPrev_Fixed_Error_Error_InvalidCache() = runTest {
        dataState = DataState.Fixed(AdditionalDataState.Error("KEY", mockk()), AdditionalDataState.Error("KEY", mockk()))
        dataCache = listOf(TestData.InvalidData)

        dataSelector.requestNextData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)

        dataSelector.requestPrevData(continueWhenError = true)
        dataState shouldBeInstanceOf DataState.Fixed::class
        (dataState as DataState.Fixed).nextDataState shouldBeInstanceOf AdditionalDataState.Error::class
        (dataState as DataState.Fixed).prevDataState shouldBeInstanceOf AdditionalDataState.Error::class
        dataCache shouldBeEqualTo listOf(TestData.InvalidData)
    }
}
