package com.kazakago.storeflowable.cacher

import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock

/**
 * This class for keeping cache in application.
 * It includes the substance of the data, the state of the data, and the request key at the time of paging.
 *
 * By default, it is cached in-memory, but if you need to save the data to another location,
 * override each save / get method.
 *
 * @see com.kazakago.storeflowable.from
 */
public abstract class Cacher<PARAM, DATA> {

    private val dataMap = mutableMapOf<PARAM, DATA?>()
    private val dataCachedAtMap = mutableMapOf<PARAM, Long>()
    private val dataStateMap = mutableMapOf<PARAM, MutableStateFlow<DataState>>()

    /**
     * Sets the time to data expire in seconds.
     * default is Long.MAX_VALUE. (= not expire)
     */
    public open val expireSeconds: Long = Long.MAX_VALUE

    /**
     * The data loading process from cache.
     *
     * @param param Key to get the specified data.
     * @return The loaded data.
     */
    public open suspend fun loadData(param: PARAM): DATA? {
        return dataMap[param]
    }

    /**
     * The data saving process to cache.
     *
     * @param data Data to be saved.
     * @param param Key to get the specified data.
     */
    public open suspend fun saveData(data: DATA?, param: PARAM) {
        dataMap[param] = data
    }

    /**
     * Gets the time when the data was cached.
     * The format is Epoch Time.
     *
     * @param param Key to get the specified data.
     */
    public open suspend fun loadDataCachedAt(param: PARAM): Long? {
        return dataCachedAtMap[param]
    }

    /**
     * Saves the time when the data was cached.
     *
     * @param epochSeconds Time when the data was cached.
     * @param param Key to get the specified data.
     */
    public open suspend fun saveDataCachedAt(epochSeconds: Long, param: PARAM) {
        dataCachedAtMap[param] = epochSeconds
    }

    /**
     * Determine if the cache is valid.
     *
     * @param cachedData Current cache data.
     * @return Returns `true` if the cache is invalid and refresh is needed.
     */
    public open suspend fun needRefresh(cachedData: DATA, param: PARAM): Boolean {
        val createdAt = loadDataCachedAt(param)
        return if (createdAt != null) {
            val expiredAt = createdAt + expireSeconds
            expiredAt < Clock.System.now().epochSeconds
        } else {
            false
        }
    }

    internal fun getStateFlow(param: PARAM): Flow<DataState> {
        return dataStateMap.getOrCreate(param)
    }

    internal fun loadState(param: PARAM): DataState {
        return dataStateMap.getOrCreate(param).value
    }

    internal fun saveState(param: PARAM, state: DataState) {
        dataStateMap.getOrCreate(param).value = state
    }

    /**
     * Clear cache.
     */
    public open fun clear() {
        dataMap.clear()
        dataCachedAtMap.clear()
        dataStateMap.clear()
    }

    private fun <KEY> MutableMap<KEY, MutableStateFlow<DataState>>.getOrCreate(key: KEY): MutableStateFlow<DataState> {
        return getOrPut(key) { MutableStateFlow(DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())) }
    }
}
