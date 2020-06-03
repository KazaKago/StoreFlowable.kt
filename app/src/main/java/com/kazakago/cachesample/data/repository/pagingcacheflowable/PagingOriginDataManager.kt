package com.kazakago.cachesample.data.repository.pagingcacheflowable

internal interface PagingOriginDataManager<DATA> {
    suspend fun fetch(data: List<DATA>?, additionalRequest: Boolean): List<DATA>
}