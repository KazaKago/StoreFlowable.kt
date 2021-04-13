package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.FetchingResult
import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.StoreFlowableResponder

interface PaginatingStoreFlowableResponder<KEY, DATA> : StoreFlowableResponder<KEY, DATA>, PaginatingCacheDataManager<DATA>, PaginatingOriginDataManager<DATA> {

    override val key: KEY

    override val flowableDataStateManager: FlowableDataStateManager<KEY>

    override suspend fun loadData(): DATA?

    override suspend fun saveData(newData: DATA?)

    override suspend fun saveAdditionalData(cachedData: DATA?, fetchedData: DATA)

    override suspend fun fetchOrigin(): FetchingResult<DATA>

    override suspend fun fetchAdditionalOrigin(cachedData: DATA?): FetchingResult<DATA>

    override suspend fun needRefresh(cachedData: DATA): Boolean
}
