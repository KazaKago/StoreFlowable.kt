package com.kazakago.storeflowable.cacher

import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock

public abstract class Cacher<PARAM, DATA> {

    private val dataMap = mutableMapOf<PARAM, DATA?>()
    private val dataCreatedAtMap = mutableMapOf<PARAM, Long>()
    private val dataStateMap = mutableMapOf<PARAM, MutableStateFlow<DataState>>()
    public open val expireSeconds: Long = Long.MAX_VALUE

    public open suspend fun loadData(param: PARAM): DATA? {
        return dataMap[param]
    }

    public open suspend fun saveData(data: DATA?, param: PARAM) {
        dataMap[param] = data
    }

    public open suspend fun loadDataCreatedAt(param: PARAM): Long? {
        return dataCreatedAtMap[param]
    }

    public open suspend fun saveDataCreatedAt(time: Long, param: PARAM) {
        dataCreatedAtMap[param] = time
    }

    public open suspend fun needRefresh(cachedData: DATA, param: PARAM): Boolean {
        val createdAt = loadDataCreatedAt(param)
        return if (createdAt != null) {
            val expiredAt = createdAt + expireSeconds
            expiredAt < Clock.System.now().epochSeconds
        } else {
            false
        }
    }

    public open fun getStateFlow(param: PARAM): Flow<DataState> {
        return dataStateMap.getOrCreate(param)
    }

    public open fun loadState(param: PARAM): DataState {
        return dataStateMap.getOrCreate(param).value
    }

    public open fun saveState(param: PARAM, state: DataState) {
        dataStateMap.getOrCreate(param).value = state
    }

    private fun <KEY> MutableMap<KEY, MutableStateFlow<DataState>>.getOrCreate(key: KEY): MutableStateFlow<DataState> {
        return getOrPut(key) { MutableStateFlow(DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())) }
    }
}
