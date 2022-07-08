package com.kazakago.storeflowable.pagination.twoway

/**
 * Result of initial fetching from origin.
 *
 * @param DATA Specify the type of data to be handled.
 */
public data class FetchedInitial<DATA>(
    /**
     * Set the acquired raw data.
     */
    val data: DATA,
    /**
     * Set the key to fetch the next data. For example, "Next page number" and "Next page token", etc...
     * If `null` or `empty` is set, it is considered that there is no next page.
     */
    val nextKey: String?,
    /**
     * Set the key to fetch the previous data. For example, "Previous page number" and "Previous page token", etc...
     * If `null` or `empty` is set, it is considered that there is no previous page.
     */
    val prevKey: String?,
)
