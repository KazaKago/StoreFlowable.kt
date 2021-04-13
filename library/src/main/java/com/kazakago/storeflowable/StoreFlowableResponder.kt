package com.kazakago.storeflowable

interface StoreFlowableResponder<KEY, DATA> : CacheDataManager<DATA>, OriginDataManager<DATA> {

    val key: KEY

    val flowableDataStateManager: FlowableDataStateManager<KEY>

    override suspend fun loadData(): DATA?

    override suspend fun saveData(newData: DATA?)

    override suspend fun fetchOrigin(): FetchingResult<DATA>

    suspend fun needRefresh(cachedData: DATA): Boolean
}
