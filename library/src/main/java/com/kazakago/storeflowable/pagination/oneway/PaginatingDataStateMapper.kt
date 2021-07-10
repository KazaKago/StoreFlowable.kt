package com.kazakago.storeflowable.pagination.oneway

import com.kazakago.storeflowable.core.pagination.AdditionalState
import com.kazakago.storeflowable.core.pagination.oneway.PaginatingState
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState

internal fun <DATA> DataState.toPaginatingState(content: DATA?): PaginatingState<DATA> {
    return when (this) {
        is DataState.Fixed -> when (appendingDataState) {
            is AdditionalDataState.Fixed -> if (content != null) {
                PaginatingState.Completed(content, AdditionalState.Fixed(noMoreAdditionalData = false))
            } else {
                PaginatingState.Loading(content)
            }
            is AdditionalDataState.FixedWithNoMoreData -> if (content != null) {
                PaginatingState.Completed(content, AdditionalState.Fixed(noMoreAdditionalData = true))
            } else {
                PaginatingState.Loading(content)
            }
            is AdditionalDataState.Loading -> if (content != null) {
                PaginatingState.Completed(content, AdditionalState.Loading)
            } else {
                PaginatingState.Loading(content)
            }
            is AdditionalDataState.Error -> if (content != null) {
                PaginatingState.Completed(content, AdditionalState.Error(appendingDataState.exception))
            } else {
                PaginatingState.Error(appendingDataState.exception)
            }
        }
        is DataState.Loading -> PaginatingState.Loading(content)
        is DataState.Error -> PaginatingState.Error(exception)
    }
}
