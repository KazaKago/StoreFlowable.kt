package com.kazakago.storeflowable.datastate

import kotlinx.coroutines.flow.Flow

internal interface DataStateFlowAccessor<PARAM> {

    fun getFlow(param: PARAM): Flow<DataState>
}
