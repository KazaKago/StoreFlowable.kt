package com.kazakago.cachesample.data.repository.pagingcacheflowable

import com.kazakago.cachesample.data.cache.state.PagingDataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

internal abstract class PagingFlowableDataStateManager<KEY> : PagingDataStateManager<KEY>, PagingFlowAccessor<KEY> {

    private val dataState: HashMap<KEY, MutableStateFlow<PagingDataState>> = hashMapOf()

    override fun getFlow(key: KEY): Flow<PagingDataState> {
        return dataState.getOrCreate(key)
    }

    override fun load(key: KEY): PagingDataState {
        return dataState.getOrCreate(key).value
    }

    override fun save(key: KEY, state: PagingDataState) {
        dataState.getOrCreate(key).value = state
    }

    private fun <KEY> HashMap<KEY, MutableStateFlow<PagingDataState>>.getOrCreate(key: KEY): MutableStateFlow<PagingDataState> {
        return getOrPut(key, { MutableStateFlow(PagingDataState.Fixed(false)) })
    }

}