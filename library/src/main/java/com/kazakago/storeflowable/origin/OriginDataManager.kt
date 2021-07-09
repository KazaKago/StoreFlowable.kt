package com.kazakago.storeflowable.origin

internal interface OriginDataManager<DATA> {

    suspend fun fetch(): InternalFetchingResult<DATA>

    suspend fun fetchAppending(cachedData: DATA?): InternalFetchingResult<DATA>

    suspend fun fetchPrepending(cachedData: DATA?): InternalFetchingResult<DATA>
}
