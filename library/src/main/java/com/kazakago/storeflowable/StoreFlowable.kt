package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.FlowableState
import com.kazakago.storeflowable.core.State
import kotlinx.coroutines.flow.Flow

/**
 * Provides input / output methods that abstract the data acquisition destination.
 *
 * This class is generated from [StoreFlowableCallback.create].
 *
 * @param KEY Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
interface StoreFlowable<KEY, DATA> {

    /**
     * Returns a [FlowableState] that can continuously receive changes in the state of the data.
     *
     * If the data has not been acquired yet, new data will be automatically acquired when this [Flow] is collected.
     *
     * The error when retrieving data is included in [State.Error].
     * and this method itself does not throw an [Exception].
     *
     * @param forceRefresh Set to `true` if you want to forcibly retrieve data from origin when collecting. Default value is `false`.
     * @return Returns a [Flow] containing the state of the data.
     */
    fun publish(forceRefresh: Boolean = false): FlowableState<DATA>

    /**
     * Deprecated, use [publish].
     *
     * @see publish
     */
    @Deprecated("Use publish", ReplaceWith("publish(forceRefresh)"))
    fun asFlow(forceRefresh: Boolean = false): Flow<State<DATA>> = publish()

    /**
     * Returns valid data only once.
     *
     * If the data could not be retrieved, it returns null instead.
     * and this method itself does not throw an [Exception].
     *
     * Use [publish] if the state of your data is likely to change.
     *
     * @param from Specifies where to get the data. Default value is [GettingFrom.Mix].
     * @return Returns the entity of the data.
     * @see GettingFrom
     * @see requireData
     */
    suspend fun getData(from: GettingFrom = GettingFrom.Mix): DATA?

    /**
     * Returns valid data only once.
     *
     * If the data cannot be acquired, an [Exception] will be thrown.
     *
     * Use [publish] if the state of your data is likely to change.
     *
     * @param from Specifies where to get the data. Default value is [GettingFrom.Mix].
     * @return Returns the entity of the data.
     * @see GettingFrom
     * @see getData
     */
    suspend fun requireData(from: GettingFrom = GettingFrom.Mix): DATA

    /**
     * Deprecated, use [requireData].
     *
     * @see requireData
     */
    @Deprecated("Use requireData()", ReplaceWith("requireData(type)"))
    suspend fun get(type: AsDataType = AsDataType.Mix): DATA = requireData(type)

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
     *
     * @param clearCacheWhenFetchFails Delete cache if data refresh fails. Default value is `true`.
     * @param continueWhenError Even if the data state is an [State.Error] when [refresh] is called, the refresh will continue. Default value is `true`.
     */
    suspend fun refresh(clearCacheWhenFetchFails: Boolean = true, continueWhenError: Boolean = true)

    /**
     * Deprecated, use [refresh].
     *
     * @see refresh
     */
    @Deprecated("Use refresh", ReplaceWith("refresh()"))
    suspend fun request() = refresh()

    /**
     * Treat the passed data as the latest acquired data.
     * and the new data will be notified.
     *
     * Use when new data is created or acquired externally.
     *
     * @param newData Latest data.
     */
    suspend fun update(newData: DATA?)
}
