package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.State
import com.kazakago.storeflowable.datastate.DataState

internal fun <DATA> DataState.mapState(content: DATA?): State<DATA> {
    return when (this) {
        is DataState.Fixed -> if (content != null) {
            State.Completed(content)
        } else {
            State.Loading()
        }
        is DataState.Loading -> if (content != null) {
            State.Refreshing(content)
        } else {
            State.Loading()
        }
        is DataState.Error -> State.Error(exception)
    }
}
