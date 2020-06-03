package com.kazakago.cacheflowable

import kotlinx.coroutines.flow.Flow

interface FlowAccessor<KEY> {
    fun getFlow(key: KEY): Flow<DataState>
}