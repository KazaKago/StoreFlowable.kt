package com.kazakago.storeflowable

/**
 * Abstract factory class for [StoreFlowable] class.
 *
 * Create a class that implements origin or cache data Input / Output according to this interface.
 *
 * @param KEY Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
interface StoreFlowableFactory<KEY, DATA> : BaseStoreFlowableFactory<KEY, DATA> {

    /**
     * The latest data acquisition process from origin.
     *
     * @return acquired data.
     */
    suspend fun fetchDataFromOrigin(): DATA
}
