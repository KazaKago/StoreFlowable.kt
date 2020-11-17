package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.State
import com.kazakago.storeflowable.core.StateContent

internal fun <DATA> DataState.mapState(stateContent: StateContent<DATA>): State<DATA> {
    return when (this) {
        is DataState.Fixed -> State.Fixed(stateContent)
        is DataState.Loading -> State.Loading(stateContent)
        is DataState.Error -> State.Error(stateContent, this.exception)
    }
}
