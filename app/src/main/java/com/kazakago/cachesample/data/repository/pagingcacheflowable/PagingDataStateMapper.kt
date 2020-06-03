package com.kazakago.cachesample.data.repository.pagingcacheflowable

import com.kazakago.cachesample.data.cache.state.PagingDataState
import com.kazakago.cachesample.domain.model.state.State
import com.kazakago.cachesample.domain.model.state.StateContent

internal fun <DATA> PagingDataState.mapState(stateContent: StateContent<DATA>): State<DATA> {
    return when (this) {
        is PagingDataState.Fixed -> State.Fixed(stateContent)
        is PagingDataState.Loading -> State.Loading(stateContent)
        is PagingDataState.Error -> State.Error(stateContent, this.exception)
    }
}
