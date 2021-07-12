package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.AdditionalLoadingState
import com.kazakago.storeflowable.core.LoadingState
import com.kazakago.storeflowable.datastate.AdditionalDataState
import com.kazakago.storeflowable.datastate.DataState

internal fun <DATA> DataState.toState(content: DATA?): LoadingState<DATA> {
    return when (this) {
        is DataState.Fixed -> when (appendingDataState) {
            is AdditionalDataState.Fixed -> if (content != null) {
                val appendingState = AdditionalLoadingState.Fixed(noMoreAdditionalData = false)
                when (prependingDataState) {
                    is AdditionalDataState.Fixed -> LoadingState.Completed(content, appendingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = false))
                    is AdditionalDataState.FixedWithNoMoreAdditionalData -> LoadingState.Completed(content, appendingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = true))
                    is AdditionalDataState.Loading -> LoadingState.Completed(content, appendingState, AdditionalLoadingState.Loading)
                    is AdditionalDataState.Error -> LoadingState.Completed(content, appendingState, AdditionalLoadingState.Error(prependingDataState.exception))
                }
            } else {
                LoadingState.Loading(content)
            }
            is AdditionalDataState.FixedWithNoMoreAdditionalData -> if (content != null) {
                val appendingState = AdditionalLoadingState.Fixed(noMoreAdditionalData = true)
                when (prependingDataState) {
                    is AdditionalDataState.Fixed -> LoadingState.Completed(content, appendingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = false))
                    is AdditionalDataState.FixedWithNoMoreAdditionalData -> LoadingState.Completed(content, appendingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = true))
                    is AdditionalDataState.Loading -> LoadingState.Completed(content, appendingState, AdditionalLoadingState.Loading)
                    is AdditionalDataState.Error -> LoadingState.Completed(content, appendingState, AdditionalLoadingState.Error(prependingDataState.exception))
                }
            } else {
                LoadingState.Loading(content)
            }
            is AdditionalDataState.Loading -> if (content != null) {
                val appendingState = AdditionalLoadingState.Loading
                when (prependingDataState) {
                    is AdditionalDataState.Fixed -> LoadingState.Completed(content, appendingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = false))
                    is AdditionalDataState.FixedWithNoMoreAdditionalData -> LoadingState.Completed(content, appendingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = true))
                    is AdditionalDataState.Loading -> LoadingState.Completed(content, appendingState, AdditionalLoadingState.Loading)
                    is AdditionalDataState.Error -> LoadingState.Completed(content, appendingState, AdditionalLoadingState.Error(prependingDataState.exception))
                }
            } else {
                LoadingState.Loading(content)
            }
            is AdditionalDataState.Error -> if (content != null) {
                val prependingState = AdditionalLoadingState.Error(appendingDataState.exception)
                when (prependingDataState) {
                    is AdditionalDataState.Fixed -> LoadingState.Completed(content, prependingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = false))
                    is AdditionalDataState.FixedWithNoMoreAdditionalData -> LoadingState.Completed(content, prependingState, AdditionalLoadingState.Fixed(noMoreAdditionalData = true))
                    is AdditionalDataState.Loading -> LoadingState.Completed(content, prependingState, AdditionalLoadingState.Loading)
                    is AdditionalDataState.Error -> LoadingState.Completed(content, prependingState, AdditionalLoadingState.Error(prependingDataState.exception))
                }
            } else {
                LoadingState.Error(appendingDataState.exception)
            }
        }
        is DataState.Loading -> LoadingState.Loading(content)
        is DataState.Error -> LoadingState.Error(exception)
    }
}
