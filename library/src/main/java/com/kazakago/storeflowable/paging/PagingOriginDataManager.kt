package com.kazakago.storeflowable.paging

internal interface PagingOriginDataManager<DATA> {
    suspend fun fetch(data: List<DATA>?, additionalRequest: Boolean): List<DATA>
}