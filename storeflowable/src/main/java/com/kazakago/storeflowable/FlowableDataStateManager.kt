package com.kazakago.storeflowable

import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.DataStateManager
import com.kazakago.storeflowable.datastate.FlowAccessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * This class that controls and holds the state of data.
 *
 * Does not handle the raw data in this class.
 *
 * @param KEY Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 */
abstract class FlowableDataStateManager<KEY> : DataStateManager<KEY>, FlowAccessor<KEY> {

    private val dataState = mutableMapOf<KEY, MutableStateFlow<DataState>>()

    /**
     * Get the data state as [Flow].
     *
     * @param key Key to get the specified data.
     * @return Flow for getting data state changes.
     */
    override fun getFlow(key: KEY): Flow<DataState> {
        return dataState.getOrCreate(key)
    }

    /**
     * Get the current data state.
     *
     * @param key Key to get the specified data.
     * @return State of saved data.
     */
    override fun load(key: KEY): DataState {
        return dataState.getOrCreate(key).value
    }

    /**
     * Save the data state.
     *
     * @param key Key to get the specified data.
     * @param state State of saved data.
     */
    override fun save(key: KEY, state: DataState) {
        dataState.getOrCreate(key).value = state
    }

    /**
     * Clear all data state in this manager.
     */
    fun clearAll() {
        dataState.clear()
    }

    private fun <KEY> MutableMap<KEY, MutableStateFlow<DataState>>.getOrCreate(key: KEY): MutableStateFlow<DataState> {
        return getOrPut(key, { MutableStateFlow(DataState.Fixed(nextDataState = AdditionalDataState.FixedWithNoMoreAdditionalData(), prevDataState = AdditionalDataState.FixedWithNoMoreAdditionalData())) })
    }
}
