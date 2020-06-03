package com.kazakago.cacheflowable

interface DataStateManager<KEY> {
    fun load(key: KEY): DataState
    fun save(key: KEY, state: DataState)
}