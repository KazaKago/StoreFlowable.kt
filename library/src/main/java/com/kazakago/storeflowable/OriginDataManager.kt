package com.kazakago.storeflowable

interface OriginDataManager<DATA> {

    suspend fun fetchDataFromOrigin(): FetchingResult<DATA>
}
