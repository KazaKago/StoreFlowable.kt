package com.kazakago.storeflowable.datastate

internal interface DataStateManager<KEY> {

    fun load(key: KEY): DataState

    fun save(key: KEY, state: DataState)
}
