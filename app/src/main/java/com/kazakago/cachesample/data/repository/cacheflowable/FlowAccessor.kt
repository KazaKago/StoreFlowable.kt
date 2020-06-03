package com.kazakago.cachesample.data.repository.cacheflowable

import com.kazakago.cachesample.data.cache.state.DataState
import kotlinx.coroutines.flow.Flow

internal interface FlowAccessor<KEY> {
    fun getFlow(key: KEY): Flow<DataState>
}