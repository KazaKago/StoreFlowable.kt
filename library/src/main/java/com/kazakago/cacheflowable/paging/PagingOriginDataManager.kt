package com.kazakago.cacheflowable.paging

interface PagingOriginDataManager<DATA> {
    suspend fun fetch(data: List<DATA>?, additionalRequest: Boolean): List<DATA>
}