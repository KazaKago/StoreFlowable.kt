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
 * @param PARAM Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 */
abstract class FlowableDataStateManager<PARAM> : DataStateManager<PARAM>, FlowAccessor<PARAM> {

    private val dataState = mutableMapOf<PARAM, MutableStateFlow<DataState>>()

    /**
     * Get the data state as [Flow].
     *
     * @param param Key to get the specified data.
     * @return Flow for getting data state changes.
     */
    override fun getFlow(param: PARAM): Flow<DataState> {
        return dataState.getOrCreate(param)
    }

    /**
     * Get the current data state.
     *
     * @param param Key to get the specified data.
     * @return State of saved data.
     */
    override fun load(param: PARAM): DataState {
        return dataState.getOrCreate(param).value
    }

    /**
     * Save the data state.
     *
     * @param param Key to get the specified data.
     * @param state State of saved data.
     */
    override fun save(param: PARAM, state: DataState) {
        dataState.getOrCreate(param).value = state
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
