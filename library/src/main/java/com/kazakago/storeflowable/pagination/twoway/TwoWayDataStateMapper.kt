package com.kazakago.storeflowable.pagination.twoway

import com.kazakago.storeflowable.core.pagination.AdditionalLoadingState
import com.kazakago.storeflowable.core.pagination.twoway.TwoWayLoadingState
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState

internal fun <DATA> DataState.toTwoWayLoadingState(content: DATA?): TwoWayLoadingState<DATA> {
    return when (this) {
        is DataState.Fixed -> if (content != null) {
            when (appendingDataState) {
                is AdditionalDataState.Fixed -> {
                    val appendingState = AdditionalLoadingState.Fixed(noMoreAdditionalData = false)
                    when (prependingDataState) {
                        is AdditionalDataState.Fixed -> TwoWayLoadingState.Completed(content, appendingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = false))
                        is AdditionalDataState.FixedWithNoMoreAdditionalData -> TwoWayLoadingState.Completed(content, appendingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = true))
                        is AdditionalDataState.Loading -> TwoWayLoadingState.Completed(content, appendingState, AdditionalLoadingState.Loading)
                        is AdditionalDataState.Error -> TwoWayLoadingState.Completed(content, appendingState, AdditionalLoadingState.Error(prependingDataState.exception))
                    }
                }
                is AdditionalDataState.FixedWithNoMoreAdditionalData -> {
                    val appendingState = AdditionalLoadingState.Fixed(noMoreAdditionalData = true)
                    when (prependingDataState) {
                        is AdditionalDataState.Fixed -> TwoWayLoadingState.Completed(content, appendingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = false))
                        is AdditionalDataState.FixedWithNoMoreAdditionalData -> TwoWayLoadingState.Completed(content, appendingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = true))
                        is AdditionalDataState.Loading -> TwoWayLoadingState.Completed(content, appendingState, AdditionalLoadingState.Loading)
                        is AdditionalDataState.Error -> TwoWayLoadingState.Completed(content, appendingState, AdditionalLoadingState.Error(prependingDataState.exception))
                    }
                }
                is AdditionalDataState.Loading -> {
                    val appendingState = AdditionalLoadingState.Loading
                    when (prependingDataState) {
                        is AdditionalDataState.Fixed -> TwoWayLoadingState.Completed(content, appendingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = false))
                        is AdditionalDataState.FixedWithNoMoreAdditionalData -> TwoWayLoadingState.Completed(content, appendingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = true))
                        is AdditionalDataState.Loading -> TwoWayLoadingState.Completed(content, appendingState, AdditionalLoadingState.Loading)
                        is AdditionalDataState.Error -> TwoWayLoadingState.Completed(content, appendingState, AdditionalLoadingState.Error(prependingDataState.exception))
                    }
                }
                is AdditionalDataState.Error -> {
                    val prependingState = AdditionalLoadingState.Error(appendingDataState.exception)
                    when (prependingDataState) {
                        is AdditionalDataState.Fixed -> TwoWayLoadingState.Completed(content, prependingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = false))
                        is AdditionalDataState.FixedWithNoMoreAdditionalData -> TwoWayLoadingState.Completed(content, prependingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = true))
                        is AdditionalDataState.Loading -> TwoWayLoadingState.Completed(content, prependingState, AdditionalLoadingState.Loading)
                        is AdditionalDataState.Error -> TwoWayLoadingState.Completed(content, prependingState, AdditionalLoadingState.Error(prependingDataState.exception))
                    }
                }
            }
        } else {
            TwoWayLoadingState.Loading(content)
        }
        is DataState.Loading -> TwoWayLoadingState.Loading(content)
        is DataState.Error -> TwoWayLoadingState.Error(exception)
    }
}
