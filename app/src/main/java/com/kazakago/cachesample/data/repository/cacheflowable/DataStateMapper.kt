package com.kazakago.cachesample.data.repository.cacheflowable

import com.kazakago.cachesample.data.cache.state.DataState
import com.kazakago.cachesample.domain.model.state.State
import com.kazakago.cachesample.domain.model.state.StateContent

internal fun <DATA> DataState.mapState(stateContent: StateContent<DATA>): State<DATA> {
    return when (this) {
        is DataState.Fixed -> State.Fixed(stateContent)
        is DataState.Loading -> State.Loading(stateContent)
        is DataState.Error -> State.Error(stateContent, this.exception)
    }
}
