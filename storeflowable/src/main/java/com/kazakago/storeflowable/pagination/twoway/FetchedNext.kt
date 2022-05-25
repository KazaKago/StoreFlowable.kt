package com.kazakago.storeflowable.pagination.twoway

/**
 * Result of next fetching from origin.
 *
 * @param DATA Specify the type of data to be handled.
 */
public data class FetchedNext<DATA>(
    /**
     * Set the acquired raw data.
     */
    val data: DATA,
    /**
     * Set the key to fetch the next data. For example, "Next page number" and "Next page token", etc...
     * If `null` or `empty` is set, it is considered that there is no next page.
     */
    val nextKey: String?,
)
