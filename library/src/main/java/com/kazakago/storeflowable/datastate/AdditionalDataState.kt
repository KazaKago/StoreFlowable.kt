package com.kazakago.storeflowable.datastate

@Suppress("CanSealedSubClassBeObject")
internal sealed interface AdditionalDataState {
    class Fixed : AdditionalDataState
    class FixedWithNoMoreAdditionalData : AdditionalDataState
    class Loading : AdditionalDataState
    class Error(val exception: Exception) : AdditionalDataState
}
