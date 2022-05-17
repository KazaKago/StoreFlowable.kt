package com.kazakago.storeflowable.cacher

import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
public abstract class Cacher<PARAM, DATA> {

    private val dataMap = mutableMapOf<PARAM, DATA?>()
    private val dataCreatedAtMap = mutableMapOf<PARAM, Instant>()
    private val dataStateMap = mutableMapOf<PARAM, MutableStateFlow<DataState>>()
    public open val expireTime: Duration = Duration.INFINITE

    public open suspend fun loadData(param: PARAM): DATA? {
        return dataMap[param]
    }

    public open suspend fun saveData(data: DATA?, param: PARAM) {
        dataMap[param] = data
    }

    public open suspend fun loadDataCreatedAt(param: PARAM): Instant? {
        return dataCreatedAtMap[param]
    }

    public open suspend fun saveDataCreatedAt(time: Instant, param: PARAM) {
        dataCreatedAtMap[param] = time
    }

    public open suspend fun needRefresh(cachedData: DATA, param: PARAM): Boolean {
        val createdAt = loadDataCreatedAt(param)
        return if (createdAt != null) {
            val expiredAt = createdAt + expireTime
            expiredAt < Clock.System.now()
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

    private fun <KEY> MutableMap<KEY, MutableStateFlow<DataState>>.getOrCreate(key: KEY): MutableStateFlow<DataState> {
        return getOrPut(key) { MutableStateFlow(DataState.Fixed(AdditionalDataState.Fixed(), AdditionalDataState.Fixed())) }
    }
}
