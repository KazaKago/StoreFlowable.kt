package com.kazakago.storeflowable.fetcher

/**
 * A Fetcher class that supports pagination in two direction.
 *
 * @see com.kazakago.storeflowable.from
 */
public interface TwoWayPaginationFetcher<PARAM, DATA> {

    /**
     * The latest data acquisition process from origin.
     *
     * @return [Result] class including the acquired data.
     */
    public suspend fun fetch(param: PARAM): Result.Initial<DATA>

    /**
     * Next data acquisition process from origin.
     *
     * @param nextKey Key for next data request.
     * @return [Result] class including the acquired data.
     */
    public suspend fun fetchNext(nextKey: String, param: PARAM): Result.Next<DATA>

    /**
     * Previous data acquisition process from origin.
     *
     * @param prevKey Key for previous data request.
     * @return [Fetched] class including the acquired data.
     */
    public suspend fun fetchPrev(prevKey: String, param: PARAM): Result.Prev<DATA>

    /**
     * Result of Fetching from origin.
     *
     * @param DATA Specify the type of data to be handled.
     */
    public sealed interface Result<DATA> {
        /**
         * Set the acquired raw data.
         */
        public val data: List<DATA>

        /**
         * Result of initial fetching from origin.
         *
         * @param DATA Specify the type of data to be handled.
         */
        public data class Initial<DATA>(
            override val data: List<DATA>,
            /**
             * Set the key to fetch the next data. For example, "Next page number" and "Next page token", etc...
             * If `null` or `empty` is set, it is considered that there is no next page.
             */
            val nextRequestKey: String?,
            /**
             * Set the key to fetch the previous data. For example, "Prev page number" and "Prev page token", etc...
             * If `null` or `empty` is set, it is considered that there is no previous page.
             */
            val prevRequestKey: String?,
        ) : Result<DATA>

        /**
         * Result of next fetching from origin.
         *
         * @param DATA Specify the type of data to be handled.
         */
        public data class Next<DATA>(
            override val data: List<DATA>,
            /**
             * Set the key to fetch the next data. For example, "Next page number" and "Next page token", etc...
             * If `null` or `empty` is set, it is considered that there is no next page.
             */
            val nextRequestKey: String?,
        ) : Result<DATA>

        /**
         * Result of previous fetching from origin.
         *
         * @param DATA Specify the type of data to be handled.
         */
        public data class Prev<DATA>(
            override val data: List<DATA>,
            /**
             * Set the key to fetch the previous data. For example, "Prev page number" and "Prev page token", etc...
             * If `null` or `empty` is set, it is considered that there is no previous page.
             */
            val prevRequestKey: String?,
        ) : Result<DATA>
    }
}