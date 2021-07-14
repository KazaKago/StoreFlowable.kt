package com.kazakago.storeflowable.origin

internal interface OriginDataManager<DATA> {

    suspend fun fetch(): InternalFetchingResult<DATA>

    suspend fun fetchNext(nextKey: String): InternalFetchingResult<DATA>

    suspend fun fetchPrev(prevKey: String): InternalFetchingResult<DATA>
}
