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
    Mix,
    /**
     * Always try to get data from origin.
     */
    FromOrigin,
    /**
     * Always try to get data from cache.
     */
    FromCache,
}
