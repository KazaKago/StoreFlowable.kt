package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.LoadingState
import com.kazakago.storeflowable.datastate.DataState

internal fun <DATA> DataState.toLoadingState(content: DATA?): LoadingState<DATA> {
    return when (this) {
        is DataState.Fixed -> if (content != null) {
            LoadingState.Completed(content)
        } else {
            LoadingState.Loading(content)
        }
        is DataState.Loading -> LoadingState.Loading(content)
        is DataState.Error -> LoadingState.Error(exception)
    }
}
