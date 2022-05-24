package com.kazakago.storeflowable

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.cache.RequestKeyManager
import com.kazakago.storeflowable.cacher.Cacher
import com.kazakago.storeflowable.cacher.PaginationCacher
import com.kazakago.storeflowable.cacher.TwoWayPaginationCacher
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.DataStateFlowAccessor
import com.kazakago.storeflowable.datastate.DataStateManager
import com.kazakago.storeflowable.fetcher.Fetcher
import com.kazakago.storeflowable.fetcher.PaginationFetcher
import com.kazakago.storeflowable.fetcher.TwoWayPaginationFetcher
import com.kazakago.storeflowable.logic.StoreFlowableImpl
import com.kazakago.storeflowable.origin.InternalFetched
import com.kazakago.storeflowable.origin.OriginDataManager
import com.kazakago.storeflowable.pagination.oneway.PaginationStoreFlowable
import com.kazakago.storeflowable.pagination.twoway.TwoWayPaginationStoreFlowable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

/**
 * Create [StoreFlowable] class from [StoreFlowableFactory].
 *
 * @return Created StateFlowable.
 */
public fun <PARAM, DATA> StoreFlowableFactory<PARAM, DATA>.create(
    param: PARAM,
    asyncDispatcher: CoroutineDispatcher = defaultAsyncDispatcher,
): StoreFlowable<DATA> {
    return StoreFlowableImpl(
        dataStateFlowAccessor = object : DataStateFlowAccessor {
            override fun getFlow() = flowableDataStateManager.getFlow(param)
        },
        requestKeyManager = object : RequestKeyManager {
            override suspend fun loadNext(): String? = null
            override suspend fun saveNext(requestKey: String?) {}
            override suspend fun loadPrev(): String? = null
            override suspend fun savePrev(requestKey: String?) {}
        },
        cacheDataManager = object : CacheDataManager<DATA> {
            override suspend fun load() = loadDataFromCache(param)
            override suspend fun save(newData: DATA?) = saveDataToCache(newData, param)
            override suspend fun saveNext(cachedData: DATA, newData: DATA) = throw NotImplementedError()
            override suspend fun savePrev(cachedData: DATA, newData: DATA) = throw NotImplementedError()
        },
        originDataManager = object : OriginDataManager<DATA> {
            override suspend fun fetch(): InternalFetched<DATA> {
                val data = fetchDataFromOrigin(param)
                return InternalFetched(data = data, nextKey = null, prevKey = null)
            }

            override suspend fun fetchNext(nextKey: String) = throw NotImplementedError()
            override suspend fun fetchPrev(prevKey: String) = throw NotImplementedError()
        },
        dataStateManager = object : DataStateManager {
            override fun load() = flowableDataStateManager.load(param)
            override fun save(state: DataState) = flowableDataStateManager.save(param, state)
        },
        needRefresh = { needRefresh(it, param) },
        asyncDispatcher = asyncDispatcher,
    )
}

/**
 * Create [StoreFlowable] class from [Cacher] & [Fetcher].
 *
 * @return Created StateFlowable.
 * @see com.kazakago.storeflowable.cacher.Cacher
 * @see com.kazakago.storeflowable.fetcher.Fetcher
 */
public fun <PARAM, DATA> StoreFlowable.Companion.from(
    fetcher: Fetcher<PARAM, DATA>,
    cacher: Cacher<PARAM, DATA>,
    param: PARAM,
    asyncDispatcher: CoroutineDispatcher = defaultAsyncDispatcher,
): StoreFlowable<DATA> {
    return StoreFlowableImpl(
        dataStateFlowAccessor = object : DataStateFlowAccessor {
            override fun getFlow(): Flow<DataState> {
                return cacher.getStateFlow(param)
            }
        },
        requestKeyManager = object : RequestKeyManager {
            override suspend fun loadNext(): String? {
                return null
            }

            override suspend fun saveNext(requestKey: String?) {
                // do nothing.
            }

            override suspend fun loadPrev(): String? {
                return null
            }

            override suspend fun savePrev(requestKey: String?) {
                // do nothing.
            }
        },
        cacheDataManager = object : CacheDataManager<DATA> {
            override suspend fun load(): DATA? {
                return cacher.loadData(param)
            }

            override suspend fun save(newData: DATA?) {
                cacher.saveData(newData, param)
                cacher.saveDataCachedAt(Clock.System.now().epochSeconds, param)
            }

            override suspend fun saveNext(cachedData: DATA, newData: DATA) {
                throw NotImplementedError()
            }

            override suspend fun savePrev(cachedData: DATA, newData: DATA) {
                throw NotImplementedError()
            }
        },
        originDataManager = object : OriginDataManager<DATA> {
            override suspend fun fetch(): InternalFetched<DATA> {
                val data = fetcher.fetch(param)
                return InternalFetched(data = data, nextKey = null, prevKey = null)
            }

            override suspend fun fetchNext(nextKey: String): InternalFetched<DATA> {
                throw NotImplementedError()
            }

            override suspend fun fetchPrev(prevKey: String): InternalFetched<DATA> {
                throw NotImplementedError()
            }
        },
        dataStateManager = object : DataStateManager {
            override fun load(): DataState {
                return cacher.loadState(param)
            }

            override fun save(state: DataState) {
                cacher.saveState(param, state)
            }
        },
        needRefresh = {
            cacher.needRefresh(it, param)
        },
        asyncDispatcher = asyncDispatcher,
    )
}

/**
 * Create [StoreFlowable] class from [PaginationCacher] & [PaginationFetcher].
 *
 * @return Created StateFlowable.
 * @see com.kazakago.storeflowable.cacher.PaginationCacher
 * @see com.kazakago.storeflowable.fetcher.PaginationFetcher
 */
public fun <PARAM, DATA> StoreFlowable.Companion.from(
    fetcher: PaginationFetcher<PARAM, DATA>,
    cacher: PaginationCacher<PARAM, DATA>,
    param: PARAM,
    asyncDispatcher: CoroutineDispatcher = defaultAsyncDispatcher,
): PaginationStoreFlowable<List<DATA>> {
    return StoreFlowableImpl(
        dataStateFlowAccessor = object : DataStateFlowAccessor {
            override fun getFlow(): Flow<DataState> {
                return cacher.getStateFlow(param)
            }
        },
        requestKeyManager = object : RequestKeyManager {
            override suspend fun loadNext(): String? {
                return cacher.loadNextRequestKey(param)
            }

            override suspend fun saveNext(requestKey: String?) {
                cacher.saveNextRequestKey(requestKey, param)
            }

            override suspend fun loadPrev(): String? {
                return null
            }

            override suspend fun savePrev(requestKey: String?) {
                // do nothing.
            }
        },
        cacheDataManager = object : CacheDataManager<List<DATA>> {
            override suspend fun load(): List<DATA>? {
                return cacher.loadData(param)
            }

            override suspend fun save(newData: List<DATA>?) {
                cacher.saveData(newData, param)
                cacher.saveDataCachedAt(Clock.System.now().epochSeconds, param)
            }

            override suspend fun saveNext(cachedData: List<DATA>, newData: List<DATA>) {
                cacher.saveNextData(cachedData, newData, param)
            }

            override suspend fun savePrev(cachedData: List<DATA>, newData: List<DATA>) {
                throw NotImplementedError()
            }
        },
        originDataManager = object : OriginDataManager<List<DATA>> {
            override suspend fun fetch(): InternalFetched<List<DATA>> {
                val result = fetcher.fetch(param)
                return InternalFetched(result.data, nextKey = result.nextRequestKey, prevKey = null)
            }

            override suspend fun fetchNext(nextKey: String): InternalFetched<List<DATA>> {
                val result = fetcher.fetchNext(nextKey, param)
                return InternalFetched(result.data, nextKey = result.nextRequestKey, prevKey = null)
            }

            override suspend fun fetchPrev(prevKey: String): InternalFetched<List<DATA>> {
                throw NotImplementedError()
            }
        },
        dataStateManager = object : DataStateManager {
            override fun load(): DataState {
                return cacher.loadState(param)
            }

            override fun save(state: DataState) {
                cacher.saveState(param, state)
            }
        },
        needRefresh = {
            cacher.needRefresh(it, param)
        },
        asyncDispatcher = asyncDispatcher,
    )
}

/**
 * Create [StoreFlowable] class from [TwoWayPaginationCacher] & [TwoWayPaginationFetcher].
 *
 * @return Created StateFlowable.
 * @see com.kazakago.storeflowable.cacher.TwoWayPaginationCacher
 * @see com.kazakago.storeflowable.fetcher.TwoWayPaginationFetcher
 */
public fun <PARAM, DATA> StoreFlowable.Companion.from(
    fetcher: TwoWayPaginationFetcher<PARAM, DATA>,
    cacher: TwoWayPaginationCacher<PARAM, DATA>,
    param: PARAM,
    asyncDispatcher: CoroutineDispatcher = defaultAsyncDispatcher,
): TwoWayPaginationStoreFlowable<List<DATA>> {
    return StoreFlowableImpl(
        dataStateFlowAccessor = object : DataStateFlowAccessor {
            override fun getFlow(): Flow<DataState> {
                return cacher.getStateFlow(param)
            }
        },
        requestKeyManager = object : RequestKeyManager {
            override suspend fun loadNext(): String? {
                return cacher.loadNextRequestKey(param)
            }

            override suspend fun saveNext(requestKey: String?) {
                cacher.saveNextRequestKey(requestKey, param)
            }

            override suspend fun loadPrev(): String? {
                return cacher.loadPrevRequestKey(param)
            }

            override suspend fun savePrev(requestKey: String?) {
                cacher.savePrevRequestKey(requestKey, param)
            }
        },
        cacheDataManager = object : CacheDataManager<List<DATA>> {
            override suspend fun load(): List<DATA>? {
                return cacher.loadData(param)
            }

            override suspend fun save(newData: List<DATA>?) {
                cacher.saveData(newData, param)
                cacher.saveDataCachedAt(Clock.System.now().epochSeconds, param)
            }

            override suspend fun saveNext(cachedData: List<DATA>, newData: List<DATA>) {
                cacher.saveNextData(cachedData, newData, param)
            }

            override suspend fun savePrev(cachedData: List<DATA>, newData: List<DATA>) {
                cacher.savePrevData(cachedData, newData, param)
            }
        },
        originDataManager = object : OriginDataManager<List<DATA>> {
            override suspend fun fetch(): InternalFetched<List<DATA>> {
                val result = fetcher.fetch(param)
                return InternalFetched(result.data, nextKey = result.nextRequestKey, prevKey = result.prevRequestKey)
            }

            override suspend fun fetchNext(nextKey: String): InternalFetched<List<DATA>> {
                val result = fetcher.fetchNext(nextKey, param)
                return InternalFetched(result.data, nextKey = result.nextRequestKey, prevKey = null)
            }

            override suspend fun fetchPrev(prevKey: String): InternalFetched<List<DATA>> {
                val result = fetcher.fetchPrev(prevKey, param)
                return InternalFetched(result.data, nextKey = null, prevKey = result.prevRequestKey)
            }
        },
        dataStateManager = object : DataStateManager {
            override fun load(): DataState {
                return cacher.loadState(param)
            }

            override fun save(state: DataState) {
                cacher.saveState(param, state)
            }
        },
        needRefresh = {
            cacher.needRefresh(it, param)
        },
        asyncDispatcher = asyncDispatcher,
    )
}
