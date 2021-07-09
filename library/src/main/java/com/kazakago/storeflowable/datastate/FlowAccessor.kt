package com.kazakago.storeflowable.datastate

import kotlinx.coroutines.flow.Flow

internal interface FlowAccessor<KEY> {

    fun getFlow(key: KEY): Flow<DataState>
}
