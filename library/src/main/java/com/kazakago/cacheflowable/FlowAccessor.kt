package com.kazakago.cacheflowable

import kotlinx.coroutines.flow.Flow

internal interface FlowAccessor<KEY> {
    fun getFlow(key: KEY): Flow<DataState>
}