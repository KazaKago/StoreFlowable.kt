package com.kazakago.storeflowable.core.pagination.oneway

sealed interface AdditionalState {
    object Loading : AdditionalState
    data class Error(val exception: Exception) : AdditionalState
}
