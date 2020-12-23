package com.kazakago.storeflowable.paging

interface PagingCacheDataManager<DATA> {

    suspend fun loadData(): List<DATA>?

    suspend fun saveData(data: List<DATA>?, additionalRequest: Boolean)
}
