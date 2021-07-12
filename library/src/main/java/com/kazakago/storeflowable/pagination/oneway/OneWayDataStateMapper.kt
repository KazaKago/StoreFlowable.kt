package com.kazakago.storeflowable.pagination.oneway

import com.kazakago.storeflowable.core.pagination.AdditionalLoadingState
import com.kazakago.storeflowable.core.pagination.oneway.OneWayLoadingState
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState

internal fun <DATA> DataState.toOneWayLoadingState(content: DATA?): OneWayLoadingState<DATA> {
    return when (this) {
        is DataState.Fixed -> if (content != null) {
            when (appendingDataState) {
                is AdditionalDataState.Fixed -> OneWayLoadingState.Completed(content, AdditionalLoadingState.Fixed(noMoreAdditionalData = false))
                is AdditionalDataState.FixedWithNoMoreAdditionalData -> OneWayLoadingState.Completed(content, AdditionalLoadingState.Fixed(noMoreAdditionalData = true))
                is AdditionalDataState.Loading -> OneWayLoadingState.Completed(content, AdditionalLoadingState.Loading)
                is AdditionalDataState.Error -> OneWayLoadingState.Completed(content, AdditionalLoadingState.Error(appendingDataState.exception))
            }
        } else {
            OneWayLoadingState.Loading(content)
        }
        is DataState.Loading -> OneWayLoadingState.Loading(content)
        is DataState.Error -> OneWayLoadingState.Error(exception)
    }
}
