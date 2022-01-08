package com.kazakago.storeflowable

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.logic.StoreFlowableImpl
import com.kazakago.storeflowable.origin.InternalFetched
import com.kazakago.storeflowable.origin.OriginDataManager
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Create [StoreFlowable] class from [StoreFlowableFactory].
 *
 * @return Created StateFlowable.
 */
fun <PARAM, DATA> StoreFlowableFactory<PARAM, DATA>.create(
    param: PARAM,
    asyncDispatcher: CoroutineDispatcher = defaultAsyncDispatcher,
): StoreFlowable<DATA> {
    return StoreFlowableImpl(
        param = param,
        flowableDataStateManager = flowableDataStateManager,
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
        needRefresh = { needRefresh(it, param) },
        asyncDispatcher = asyncDispatcher,
    )
}
