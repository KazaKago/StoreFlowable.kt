package com.kazakago.cacheflowable

internal interface DataStateManager<KEY> {
    fun load(key: KEY): DataState
    fun save(key: KEY, state: DataState)
}