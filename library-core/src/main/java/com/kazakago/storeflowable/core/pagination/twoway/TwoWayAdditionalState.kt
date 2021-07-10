package com.kazakago.storeflowable.core.pagination.twoway

sealed interface TwoWayAdditionalState {
    object Fixed : TwoWayAdditionalState
    object Loading : TwoWayAdditionalState
    data class Error(val exception: Exception) : TwoWayAdditionalState
}
