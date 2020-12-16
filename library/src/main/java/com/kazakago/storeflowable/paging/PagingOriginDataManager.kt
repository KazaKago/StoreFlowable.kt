package com.kazakago.storeflowable.paging

interface PagingOriginDataManager<DATA> {

    suspend fun fetchOrigin(data: List<DATA>?, additionalRequest: Boolean): List<DATA>
}
