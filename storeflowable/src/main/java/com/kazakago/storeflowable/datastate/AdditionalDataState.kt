package com.kazakago.storeflowable.datastate

@Suppress("CanSealedSubClassBeObject")
sealed interface AdditionalDataState {
    class Fixed : AdditionalDataState
    class Loading : AdditionalDataState
    class Error(val exception: Exception) : AdditionalDataState
}
