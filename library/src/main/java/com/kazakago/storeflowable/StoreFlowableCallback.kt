package com.kazakago.storeflowable

interface StoreFlowableCallback<KEY, DATA> : CacheDataManager<DATA>, OriginDataManager<DATA> {

    val key: KEY

    val flowableDataStateManager: FlowableDataStateManager<KEY>

    override suspend fun loadDataFromCache(): DATA?

    override suspend fun saveDataToCache(newData: DATA?)

    override suspend fun fetchDataFromOrigin(): FetchingResult<DATA>

    suspend fun needRefresh(cachedData: DATA): Boolean
}
