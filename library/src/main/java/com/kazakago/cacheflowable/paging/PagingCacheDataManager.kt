package com.kazakago.cacheflowable.paging

internal interface PagingCacheDataManager<DATA> {
    suspend fun load(): List<DATA>?
    suspend fun save(data: List<DATA>?, additionalRequest: Boolean)
}
