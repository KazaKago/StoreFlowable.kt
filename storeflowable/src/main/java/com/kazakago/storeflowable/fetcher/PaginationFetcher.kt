package com.kazakago.storeflowable.fetcher

/**
 * A Fetcher class that supports pagination in one direction.
 *
 * @see com.kazakago.storeflowable.from
 */
public interface PaginationFetcher<PARAM, DATA> {

    /**
     * The latest data acquisition process from origin.
     *
     * @return [Result] class including the acquired data.
     */
    public suspend fun fetch(param: PARAM): Result<DATA>

    /**
     * The latest data acquisition process from origin.
     *
     * @return [Result] class including the acquired data.
     */
    public suspend fun fetchNext(nextKey: String, param: PARAM): Result<DATA>

    /**
     * Result of Fetching from origin.
     *
     * @param DATA Specify the type of data to be handled.
     */
    public data class Result<DATA>(
        /**
         * Set the acquired raw data.
         */
        val data: List<DATA>,
        /**
         * Set the key to fetch the next data. For example, "Next page number" and "Next page token", etc...
         * If `null` or `empty` is set, it is considered that there is no next page.
         */
        val nextRequestKey: String?,
    )
}
