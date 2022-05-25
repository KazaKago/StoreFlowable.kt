package com.kazakago.storeflowable.pagination.twoway

/**
 * Result of previous fetching from origin.
 *
 * @param DATA Specify the type of data to be handled.
 */
@Deprecated("use Cacher class & Fetcher class")
public data class FetchedPrev<DATA>(
    /**
     * Set the acquired raw data.
     */
    val data: DATA,
    /**
     * Set the key to fetch the previous data. For example, "Prev page number" and "Prev page token", etc...
     * If `null` or `empty` is set, it is considered that there is no previous page.
     */
    val prevKey: String?,
)
