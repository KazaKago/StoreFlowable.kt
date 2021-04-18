package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.FetchingResult
import com.kazakago.storeflowable.FlowableDataStateManager

interface PaginatingStoreFlowableCallback<KEY, DATA> : PaginatingCacheDataManager<DATA>, PaginatingOriginDataManager<DATA> {

    val key: KEY

    val flowableDataStateManager: FlowableDataStateManager<KEY>

    override suspend fun loadDataFromCache(): DATA?

    override suspend fun saveDataToCache(newData: DATA?)

    override suspend fun saveAdditionalDataToCache(cachedData: DATA?, newData: DATA)

    override suspend fun fetchDataFromOrigin(): FetchingResult<DATA>

    override suspend fun fetchAdditionalDataFromOrigin(cachedData: DATA?): FetchingResult<DATA>

    suspend fun needRefresh(cachedData: DATA): Boolean
}
