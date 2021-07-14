package com.kazakago.storeflowable.pagination.twoway

import com.kazakago.storeflowable.core.pagination.AdditionalLoadingState
import com.kazakago.storeflowable.core.pagination.twoway.TwoWayLoadingState
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState

internal fun <DATA> DataState.toTwoWayLoadingState(content: DATA?): TwoWayLoadingState<DATA> {
    return when (this) {
        is DataState.Fixed -> if (content != null) {
            when (nextDataState) {
                is AdditionalDataState.Fixed -> {
                    val nextState = AdditionalLoadingState.Fixed(noMoreAdditionalData = false)
                    when (prevDataState) {
                        is AdditionalDataState.Fixed -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(noMoreAdditionalData = false))
                        is AdditionalDataState.FixedWithNoMoreAdditionalData -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(noMoreAdditionalData = true))
                        is AdditionalDataState.Loading -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Loading)
                        is AdditionalDataState.Error -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Error(prevDataState.exception))
                    }
                }
                is AdditionalDataState.FixedWithNoMoreAdditionalData -> {
                    val nextState = AdditionalLoadingState.Fixed(noMoreAdditionalData = true)
                    when (prevDataState) {
                        is AdditionalDataState.Fixed -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(noMoreAdditionalData = false))
                        is AdditionalDataState.FixedWithNoMoreAdditionalData -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(noMoreAdditionalData = true))
                        is AdditionalDataState.Loading -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Loading)
                        is AdditionalDataState.Error -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Error(prevDataState.exception))
                    }
                }
                is AdditionalDataState.Loading -> {
                    val nextState = AdditionalLoadingState.Loading
                    when (prevDataState) {
                        is AdditionalDataState.Fixed -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(noMoreAdditionalData = false))
                        is AdditionalDataState.FixedWithNoMoreAdditionalData -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(noMoreAdditionalData = true))
                        is AdditionalDataState.Loading -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Loading)
                        is AdditionalDataState.Error -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Error(prevDataState.exception))
                    }
                }
                is AdditionalDataState.Error -> {
                    val nextState = AdditionalLoadingState.Error(nextDataState.exception)
                    when (prevDataState) {
                        is AdditionalDataState.Fixed -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(noMoreAdditionalData = false))
                        is AdditionalDataState.FixedWithNoMoreAdditionalData -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Fixed(noMoreAdditionalData = true))
                        is AdditionalDataState.Loading -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Loading)
                        is AdditionalDataState.Error -> TwoWayLoadingState.Completed(content, nextState, AdditionalLoadingState.Error(prevDataState.exception))
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
