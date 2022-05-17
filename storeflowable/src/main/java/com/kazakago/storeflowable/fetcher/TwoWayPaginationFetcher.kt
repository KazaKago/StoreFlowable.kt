package com.kazakago.storeflowable.fetcher

public interface TwoWayPaginationFetcher<PARAM, DATA> {

    public suspend fun fetch(param: PARAM): Result.Initial<DATA>

    public suspend fun fetchNext(nextKey: String, param: PARAM): Result.Next<DATA>

    public suspend fun fetchPrev(prevKey: String, param: PARAM): Result.Prev<DATA>

    public sealed interface Result<DATA> {
        public val data: List<DATA>

        public data class Initial<DATA>(
            override val data: List<DATA>,
            val nextRequestKey: String?,
            val prevRequestKey: String?,
        ) : Result<DATA>

        public data class Next<DATA>(
            override val data: List<DATA>,
            val nextRequestKey: String?,
        ) : Result<DATA>

        public data class Prev<DATA>(
            override val data: List<DATA>,
            val prevRequestKey: String?,
        ) : Result<DATA>
    }
}