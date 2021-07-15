package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.core.LoadingState
import kotlinx.coroutines.flow.Flow

/**
 * Common function of [com.kazakago.storeflowable.StoreFlowable], [com.kazakago.storeflowable.pagination.oneway.OneWayStoreFlowable], [com.kazakago.storeflowable.pagination.twoway.TwoWayStoreFlowable] interfaces.
 *
 * @param KEY Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
interface BaseStoreFlowable<KEY, DATA> {

    /**
     * Returns a [FlowLoadingState] that can continuously receive changes in the state of the data.
     *
     * If the data has not been acquired yet, new data will be automatically acquired when this [Flow] is collected.
     *
     * The error when retrieving data is included in [LoadingState.Error].
     * and this method itself does not throw an [Exception].
     *
     * @param forceRefresh Set to `true` if you want to forcibly retrieve data from origin when collecting. Default value is `false`.
     * @return Returns a [Flow] containing the state of the data.
     */
    fun publish(forceRefresh: Boolean = false): FlowLoadingState<DATA>

    /**
     * Returns valid data only once.
     *
     * If the data could not be retrieved, it returns null instead.
     * and this method itself does not throw an [Exception].
     *
     * Use [publish] if the state of your data is likely to change.
     *
     * @param from Specifies where to get the data. Default value is [GettingFrom.Both].
     * @return Returns the entity of the data.
     * @see GettingFrom
     * @see requireData
     */
    suspend fun getData(from: GettingFrom = GettingFrom.Both): DATA?

    /**
     * Returns valid data only once.
     *
     * If the data cannot be acquired, an [Exception] will be thrown.
     *
     * Use [publish] if the state of your data is likely to change.
     *
     * @param from Specifies where to get the data. Default value is [GettingFrom.Both].
     * @return Returns the entity of the data.
     * @see GettingFrom
     * @see getData
     */
    suspend fun requireData(from: GettingFrom = GettingFrom.Both): DATA

    /**
     * Checks if the published data is valid.
     *
     * If it is invalid, it will be reacquired from origin.
     * and the new data will be notified.
     */
    suspend fun validate()

    /**
     * Forces a data refresh.
     * and the new data will be notified.
     */
    suspend fun refresh()
}
