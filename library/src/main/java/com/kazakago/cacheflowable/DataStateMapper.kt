package com.kazakago.cacheflowable

import com.kazakago.cacheflowable.core.State
import com.kazakago.cacheflowable.core.StateContent

internal fun <DATA> DataState.mapState(stateContent: StateContent<DATA>): State<DATA> {
    return when (this) {
        is DataState.Fixed -> State.Fixed(stateContent)
        is DataState.Loading -> State.Loading(stateContent)
        is DataState.Error -> State.Error(stateContent, this.exception)
    }
}
