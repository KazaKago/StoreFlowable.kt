package com.kazakago.storeflowable.pagination.twoway

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.logic.StoreFlowableImpl
import com.kazakago.storeflowable.origin.InternalFetched
import com.kazakago.storeflowable.origin.OriginDataManager

/**
 * Create [TwoWayPaginationStoreFlowable] class from [TwoWayPaginationStoreFlowableFactory].
 *
 * @return Created [TwoWayPaginationStoreFlowable].
 */
fun <PARAM, DATA> TwoWayPaginationStoreFlowableFactory<PARAM, DATA>.create(param: PARAM): TwoWayPaginationStoreFlowable<PARAM, DATA> {
    return StoreFlowableImpl(
        key = param,
        flowableDataStateManager = flowableDataStateManager,
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
        needRefresh = { needRefresh(it, param) }
    )
}
