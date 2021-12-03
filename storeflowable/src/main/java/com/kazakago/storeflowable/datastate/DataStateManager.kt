package com.kazakago.storeflowable.datastate

internal interface DataStateManager<PARAM> {

    fun load(param: PARAM): DataState

    fun save(param: PARAM, state: DataState)
}
