package com.kazakago.storeflowable

interface StoreFlowableResponder<KEY, DATA> : CacheDataManager<DATA>, OriginDataManager<DATA> {

    val key: KEY

    val flowableDataStateManager: FlowableDataStateManager<KEY>

    override suspend fun loadData(): DATA?

    override suspend fun saveData(data: DATA?)

    override suspend fun fetchOrigin(): DATA

    suspend fun needRefresh(data: DATA): Boolean
}
