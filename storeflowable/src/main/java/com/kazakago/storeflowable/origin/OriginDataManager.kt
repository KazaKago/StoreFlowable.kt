package com.kazakago.storeflowable.origin

internal interface OriginDataManager<DATA> {

    suspend fun fetch(): InternalFetched<DATA>

    suspend fun fetchNext(nextKey: String): InternalFetched<DATA>

    suspend fun fetchPrev(prevKey: String): InternalFetched<DATA>
}
