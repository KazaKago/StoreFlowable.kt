package com.kazakago.storeflowable.datastate

internal interface DataStateManager<KEY> {

    fun loadState(key: KEY): DataState

    fun saveState(key: KEY, state: DataState)
}
