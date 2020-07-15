package com.kazakago.cacheflowable.paging

internal interface PagingOriginDataManager<DATA> {
    suspend fun fetch(data: List<DATA>?, additionalRequest: Boolean): List<DATA>
}