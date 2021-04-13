package com.kazakago.storeflowable

interface OriginDataManager<DATA> {

    suspend fun fetchOrigin(): FetchingResult<DATA>
}
