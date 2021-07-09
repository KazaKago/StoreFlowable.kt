package com.kazakago.storeflowable.origin

/**
 * This enum to specify where to get data when getting data.
 *
 * @see com.kazakago.storeflowable.StoreFlowable.getData
 * @see com.kazakago.storeflowable.StoreFlowable.requireData
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
