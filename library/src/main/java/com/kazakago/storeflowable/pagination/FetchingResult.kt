package com.kazakago.storeflowable.pagination

/**
 * Result of Fetching from origin.
 *
 * @param DATA Specify the type of data to be handled.
 */
data class FetchingResult<DATA>(
    /**
     * Set the acquired raw data.
     */
    val data: DATA,
    /**
     * If you know at Pagination that there is no more additional data, Set to `true`.
     * Otherwise, specify `false`.
     */
    val noMoreAdditionalData: Boolean,
)
