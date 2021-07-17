package com.kazakago.storeflowable

/**
 * This enum to specify where to get data when getting data.
 *
 * @see StoreFlowable.getData
 * @see StoreFlowable.requireData
 */
enum class GettingFrom {
    /**
     * Use both origin and cache.
     * Returns a valid cache if it exists, otherwise try to get it from origin.
     */
    Both,

    /**
     * Always try to get data from origin.
     */
    Origin,

    /**
     * Always try to get data from cache.
     */
    Cache,
}
