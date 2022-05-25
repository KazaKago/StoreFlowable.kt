package com.kazakago.storeflowable.pagination.twoway

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.cache.RequestKeyManager
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.DataStateFlowAccessor
import com.kazakago.storeflowable.datastate.DataStateManager
import com.kazakago.storeflowable.defaultAsyncDispatcher
import com.kazakago.storeflowable.logic.StoreFlowableImpl
import com.kazakago.storeflowable.origin.InternalFetched
import com.kazakago.storeflowable.origin.OriginDataManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow

/**
 * Create [TwoWayPaginationStoreFlowable] class from [TwoWayPaginationStoreFlowableFactory].
 *
 * @return Created [TwoWayPaginationStoreFlowable].
 */
public fun <PARAM, DATA> TwoWayPaginationStoreFlowableFactory<PARAM, DATA>.create(
    param: PARAM,
    asyncDispatcher: CoroutineDispatcher = defaultAsyncDispatcher,
): TwoWayPaginationStoreFlowable<DATA> {
    return StoreFlowableImpl(
        dataStateFlowAccessor = object : DataStateFlowAccessor {
            override fun getFlow(): Flow<DataState> = flowableDataStateManager.getFlow(param)
        },
        requestKeyManager = object : RequestKeyManager {
            override suspend fun loadNext() = flowableDataStateManager.loadNext(param)
            override suspend fun saveNext(requestKey: String?) = flowableDataStateManager.saveNext(param, requestKey)
            override suspend fun loadPrev() = flowableDataStateManager.loadPrev(param)
            override suspend fun savePrev(requestKey: String?) = flowableDataStateManager.savePrev(param, requestKey)
        },
        cacheDataManager = object : CacheDataManager<DATA> {
            override suspend fun load() = loadDataFromCache(param)
            override suspend fun save(newData: DATA?) = saveDataToCache(newData, param)
            override suspend fun saveNext(cachedData: DATA, newData: DATA) = saveNextDataToCache(cachedData, newData, param)
            override suspend fun savePrev(cachedData: DATA, newData: DATA) = savePrevDataToCache(cachedData, newData, param)
        },
        originDataManager = object : OriginDataManager<DATA> {
            override suspend fun fetch(): InternalFetched<DATA> {
                val result = fetchDataFromOrigin(param)
                return InternalFetched(result.data, nextKey = result.nextKey, prevKey = result.prevKey)
            }

            override suspend fun fetchNext(nextKey: String): InternalFetched<DATA> {
                val result = fetchNextDataFromOrigin(nextKey, param)
                return InternalFetched(result.data, nextKey = result.nextKey, prevKey = null)
            }

            override suspend fun fetchPrev(prevKey: String): InternalFetched<DATA> {
                val result = fetchPrevDataFromOrigin(prevKey, param)
                return InternalFetched(result.data, nextKey = null, prevKey = result.prevKey)
            }
        },
        dataStateManager = object : DataStateManager {
            override fun load() = flowableDataStateManager.load(param)
            override fun save(state: DataState) = flowableDataStateManager.save(param, state)
        },
        needRefresh = { needRefresh(it, param) },
        asyncDispatcher = asyncDispatcher,
    )
}
