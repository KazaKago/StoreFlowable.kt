package com.kazakago.cacheflowable

interface DataStateManager<KEY> {
    fun save(key: KEY, state: DataState)
    fun load(key: KEY): DataState
}