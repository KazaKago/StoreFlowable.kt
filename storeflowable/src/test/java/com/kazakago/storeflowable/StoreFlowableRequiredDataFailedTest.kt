package com.kazakago.storeflowable

import com.kazakago.storeflowable.cacher.Cacher
import com.kazakago.storeflowable.fetcher.Fetcher
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
class StoreFlowableRequiredDataFailedTest {

    private enum class TestData(val needRefresh: Boolean) {
        ValidData(false),
        InvalidData(true),
        FetchedData(false),
    }

    private class TestCacher(private var cache: TestData?) : Cacher<Unit, TestData>() {
        override suspend fun loadData(param: Unit): TestData? {
            return cache
        }

        override suspend fun saveData(data: TestData?, param: Unit) {
            cache = data
        }

        override suspend fun needRefresh(cachedData: TestData, param: Unit): Boolean {
            return cachedData.needRefresh
        }
    }

    private class TestFetcher : Fetcher<Unit, TestData> {
        override suspend fun fetch(param: Unit): TestData {
            throw NoSuchElementException()
        }
    }

    @Test
    fun requiredData_Both_NoCache() = runTest {
        val storeFlowable = StoreFlowable.from(TestCacher(cache = null), TestFetcher())
        shouldThrow<NoSuchElementException> { storeFlowable.requireData(GettingFrom.Both) }
    }

    @Test
    fun requiredData_Both_ValidCache() = runTest {
        val storeFlowable = StoreFlowable.from(TestCacher(cache = TestData.ValidData), TestFetcher())
        storeFlowable.requireData(GettingFrom.Both) shouldBe TestData.ValidData
    }

    @Test
    fun requiredData_Both_InvalidCache() = runTest {
        val storeFlowable = StoreFlowable.from(TestCacher(cache = TestData.InvalidData), TestFetcher())
        shouldThrow<NoSuchElementException> { storeFlowable.requireData(GettingFrom.Both) }
    }

    @Test
    fun requiredData_Cache_NoCache() = runTest {
        val storeFlowable = StoreFlowable.from(TestCacher(cache = null), TestFetcher())
        shouldThrow<NoSuchElementException> { storeFlowable.requireData(GettingFrom.Cache) }
    }

    @Test
    fun requiredData_Cache_ValidCache() = runTest {
        val storeFlowable = StoreFlowable.from(TestCacher(cache = TestData.ValidData), TestFetcher())
        storeFlowable.requireData(GettingFrom.Cache) shouldBe TestData.ValidData
    }

    @Test
    fun requiredData_Cache_InvalidCache() = runTest {
        val storeFlowable = StoreFlowable.from(TestCacher(cache = TestData.InvalidData), TestFetcher())
        shouldThrow<NoSuchElementException> { storeFlowable.requireData(GettingFrom.Cache) }
    }

    @Test
    fun requiredData_Origin_NoCache() = runTest {
        val storeFlowable = StoreFlowable.from(TestCacher(cache = null), TestFetcher())
        shouldThrow<NoSuchElementException> { storeFlowable.requireData(GettingFrom.Origin) }
    }

    @Test
    fun requiredData_Origin_ValidCache() = runTest {
        val storeFlowable = StoreFlowable.from(TestCacher(cache = TestData.ValidData), TestFetcher())
        shouldThrow<NoSuchElementException> { storeFlowable.requireData(GettingFrom.Origin) }
    }

    @Test
    fun requiredData_Origin_InvalidCache() = runTest {
        val storeFlowable = StoreFlowable.from(TestCacher(cache = TestData.InvalidData), TestFetcher())
        shouldThrow<NoSuchElementException> { storeFlowable.requireData(GettingFrom.Origin) }
    }
}
