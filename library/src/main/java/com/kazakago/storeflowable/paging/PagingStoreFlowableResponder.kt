package com.kazakago.storeflowable.paging

import com.kazakago.storeflowable.FlowableDataStateManager

@Deprecated("Use PaginatingStoreFlowableCallback")
interface PagingStoreFlowableResponder<KEY, DATA> {

    val key: KEY

    val flowableDataStateManager: FlowableDataStateManager<KEY>

    suspend fun loadData(): List<DATA>?

    suspend fun saveData(data: List<DATA>?, additionalRequest: Boolean)

    suspend fun fetchOrigin(data: List<DATA>?, additionalRequest: Boolean): List<DATA>

    suspend fun needRefresh(data: List<DATA>): Boolean
}
