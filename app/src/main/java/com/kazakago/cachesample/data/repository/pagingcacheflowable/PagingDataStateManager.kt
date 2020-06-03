package com.kazakago.cachesample.data.repository.pagingcacheflowable

import com.kazakago.cachesample.data.cache.state.PagingDataState

internal interface PagingDataStateManager<KEY> {
    fun save(key: KEY, state: PagingDataState)
    fun load(key: KEY): PagingDataState
}