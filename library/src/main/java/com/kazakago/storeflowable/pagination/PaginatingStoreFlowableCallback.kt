package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.FetchingResult
import com.kazakago.storeflowable.FlowableDataStateManager

interface PaginatingStoreFlowableCallback<KEY, DATA> : PaginatingCacheDataManager<DATA>, PaginatingOriginDataManager<DATA> {

    val key: KEY

    val flowableDataStateManager: FlowableDataStateManager<KEY>

    override suspend fun loadData(): DATA?

    override suspend fun saveData(newData: DATA?)

    override suspend fun saveAdditionalData(cachedData: DATA?, fetchedData: DATA)

    override suspend fun fetchOrigin(): FetchingResult<DATA>

    override suspend fun fetchAdditionalOrigin(cachedData: DATA?): FetchingResult<DATA>

    suspend fun needRefresh(cachedData: DATA): Boolean
}
