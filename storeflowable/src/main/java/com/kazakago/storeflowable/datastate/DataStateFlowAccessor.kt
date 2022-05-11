package com.kazakago.storeflowable.datastate

import kotlinx.coroutines.flow.Flow

internal interface DataStateFlowAccessor {

    fun getFlow(): Flow<DataState>
}
