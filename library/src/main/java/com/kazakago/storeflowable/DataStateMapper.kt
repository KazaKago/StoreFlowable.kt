package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.State
import com.kazakago.storeflowable.datastate.DataState

internal fun <DATA> DataState.toState(content: DATA?): State<DATA> {
    return when (this) {
        is DataState.Fixed -> if (content != null) {
            State.Completed(content)
        } else {
            State.Loading(null)
        }
        is DataState.Loading -> State.Loading(content)
        is DataState.Error -> State.Error(exception)
    }
}
