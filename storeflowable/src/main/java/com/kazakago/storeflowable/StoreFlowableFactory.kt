package com.kazakago.storeflowable

/**
 * Abstract factory class for [StoreFlowable] class.
 *
 * Create a class that implements origin or cache data Input / Output according to this interface.
 *
 * @param PARAM Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
public interface StoreFlowableFactory<PARAM, DATA> : BaseStoreFlowableFactory<PARAM, DATA> {

    /**
     * The latest data acquisition process from origin.
     *
     * @return acquired data.
     */
    public suspend fun fetchDataFromOrigin(param: PARAM): DATA
}
