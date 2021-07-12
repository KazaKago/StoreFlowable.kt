package com.kazakago.storeflowable.pagination.twoway

/**
 * Result of Fetching from origin.
 *
 * @param DATA Specify the type of data to be handled.
 */
data class TwoWayFetchingResult<DATA>(
    /**
     * Set the acquired raw data.
     */
    val data: DATA,
    /**
     * If you know at Pagination that there is no more appending data, set to `true`.
     * Otherwise, set to `false`.
     */
    val noMoreAppendingData: Boolean,
    /**
     * If you know at Pagination that there is no more prepending data, set to `true`.
     * Otherwise, set to `false`.
     */
    val noMorePrependingData: Boolean,
)
