package com.kazakago.cacheflowable.paging

interface PagingCacheDataManager<DATA> {
    suspend fun load(): List<DATA>?
    suspend fun save(data: List<DATA>?, additionalRequest: Boolean)
}
