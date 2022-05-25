package com.kazakago.storeflowable

import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * This class that controls and holds the state of data.
 *
 * Does not handle the raw data in this class.
 *
 * @param PARAM Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 */
@Deprecated("use Cacher class & Fetcher class")
public abstract class FlowableDataStateManager<PARAM> {

    private val dataState = mutableMapOf<PARAM, MutableStateFlow<DataState>>()
    private val nextKey = mutableMapOf<PARAM, String?>()
    private val prevKey = mutableMapOf<PARAM, String?>()

    /**
     * Get the data state as [Flow].
     *
     * @param param Key to get the specified data.
     * @return Flow for getting data state changes.
     */
    public open fun getFlow(param: PARAM): Flow<DataState> {
        return dataState.getOrCreate(param)
    }

    /**
     * Get the current data state.
     *
     * @param param Key to get the specified data.
     * @return State of saved data.
     */
    public open fun load(param: PARAM): DataState {
        return dataState.getOrCreate(param).value
    }

    /**
     * Save the data state.
     *
     * @param param Key to get the specified data.
     * @param state State of saved data.
     */
    public open fun save(param: PARAM, state: DataState) {
        dataState.getOrCreate(param).value = state
    }

    /**
     * Clear all data state in this manager.
     */
    public fun clearAll() {
        dataState.clear()
    }

    public open suspend fun loadNext(param: PARAM): String? {
        return nextKey[param]
    }

    public open suspend fun saveNext(param: PARAM, requestKey: String?) {
        nextKey[param] = requestKey
    }

    public open suspend fun loadPrev(param: PARAM): String? {
        return prevKey[param]
    }

    public open suspend fun savePrev(param: PARAM, requestKey: String?) {
        prevKey[param] = requestKey
    }

    private fun <KEY> MutableMap<KEY, MutableStateFlow<DataState>>.getOrCreate(key: KEY): MutableStateFlow<DataState> {
        return getOrPut(key) { MutableStateFlow(DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())) }
    }
}
