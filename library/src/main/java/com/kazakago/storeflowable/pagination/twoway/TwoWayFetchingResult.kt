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
     * Set to `true` if you know at Pagination that there is no more appending data.
     */
    val noMoreAppendingData: Boolean,
    /**
     * Set to `true` if you know at Pagination that there is no more prepending data.
     */
    val noMorePrependingData: Boolean,
)
