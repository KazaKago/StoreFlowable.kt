package com.kazakago.storeflowable.datastate

internal interface DataStateManager {

    fun load(): DataState

    fun save(state: DataState)
}
