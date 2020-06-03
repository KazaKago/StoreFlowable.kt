package com.kazakago.cachesample.data.repository.pagingcacheflowable

import com.kazakago.cachesample.data.cache.state.PagingDataState
import kotlinx.coroutines.flow.Flow

internal interface PagingFlowAccessor<KEY> {
    fun getFlow(key: KEY): Flow<PagingDataState>
}