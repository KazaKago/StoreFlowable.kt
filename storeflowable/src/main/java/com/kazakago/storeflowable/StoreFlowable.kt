package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.core.LoadingState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

/**
 * Provides input / output methods that abstract the data acquisition destination.
 *
 * This class is generated from [StoreFlowableFactory.create].
 *
 * @param DATA Specify the type of data to be handled.
 */
public interface StoreFlowable<DATA> {

    public companion object

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
    @FlowPreview
    public fun publish(forceRefresh: Boolean = false): FlowLoadingState<DATA>

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
    public suspend fun getData(from: GettingFrom = GettingFrom.Both): DATA?

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
    public suspend fun requireData(from: GettingFrom = GettingFrom.Both): DATA

    /**
     * Checks if the published data is valid.
     *
     * If it is invalid, it will be reacquired from origin.
     * and the new data will be notified.
     */
    public suspend fun validate()

    /**
     * Forces a data refresh.
     * and the new data will be notified.
     */
    public suspend fun refresh()

    /**
     * Treat the passed data as the latest acquired data.
     * and the new data will be notified.
     *
     * Use when new data is created or acquired externally.
     *
     * @param newData Latest data.
     */
    public suspend fun update(newData: DATA?)

    /**
     * Clear data managed by StoreFlowable.
     * and this event will be notified.
     */
    public suspend fun clear()
}
