package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.State
import com.kazakago.storeflowable.core.StateContent

internal fun <DATA> DataState.mapState(content: StateContent<DATA>): State<DATA> {
    return when (this) {
        is DataState.Fixed -> State.Fixed(content)
        is DataState.Loading -> State.Loading(content)
        is DataState.Error -> State.Error(content, this.exception)
    }
}
