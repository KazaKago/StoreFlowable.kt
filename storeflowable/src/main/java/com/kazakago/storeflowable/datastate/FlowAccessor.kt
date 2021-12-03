package com.kazakago.storeflowable.datastate

import kotlinx.coroutines.flow.Flow

internal interface FlowAccessor<PARAM> {

    fun getFlow(param: PARAM): Flow<DataState>
}
