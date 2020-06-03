package com.kazakago.cachesample.data.repository.cacheflowable

import com.kazakago.cachesample.data.cache.state.DataState

internal interface DataStateManager<KEY> {
    fun save(key: KEY, state: DataState)
    fun load(key: KEY): DataState
}