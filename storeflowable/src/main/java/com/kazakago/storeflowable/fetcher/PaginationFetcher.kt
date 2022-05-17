package com.kazakago.storeflowable.fetcher

public interface PaginationFetcher<PARAM, DATA> {

    public suspend fun fetch(param: PARAM): Result<DATA>

    public suspend fun fetchNext(nextKey: String, param: PARAM): Result<DATA>

    public data class Result<DATA>(
        val data: List<DATA>,
        val nextRequestKey: String?,
    )
}
