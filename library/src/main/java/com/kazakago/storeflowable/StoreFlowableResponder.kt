package com.kazakago.storeflowable

@Deprecated("Use StoreFlowableFactory")
interface StoreFlowableResponder<KEY, DATA> {

    val key: KEY

    val flowableDataStateManager: FlowableDataStateManager<KEY>

    suspend fun loadData(): DATA?

    suspend fun saveData(newData: DATA?)

    suspend fun fetchOrigin(): DATA

    suspend fun needRefresh(cachedData: DATA): Boolean
}
