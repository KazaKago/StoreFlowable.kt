package com.kazakago.storeflowable.paging

import com.kazakago.storeflowable.FlowableDataStateManager

interface PagingStoreFlowableResponder<KEY, DATA> : PagingCacheDataManager<DATA>, PagingOriginDataManager<DATA> {

    val key: KEY

    val flowableDataStateManager: FlowableDataStateManager<KEY>

    override suspend fun loadData(): List<DATA>?

    override suspend fun saveData(data: List<DATA>?, additionalRequest: Boolean)

    override suspend fun fetchOrigin(data: List<DATA>?, additionalRequest: Boolean): List<DATA>

    suspend fun needRefresh(data: List<DATA>): Boolean
}
