package com.kazakago.storeflowable.datastate

@Suppress("CanSealedSubClassBeObject")
internal sealed interface AdditionalDataState {
    class Fixed(val additionalRequestKey: String) : AdditionalDataState
    class FixedWithNoMoreAdditionalData : AdditionalDataState
    class Loading(val additionalRequestKey: String) : AdditionalDataState
    class Error(val additionalRequestKey: String, val exception: Exception) : AdditionalDataState

    fun additionalRequestKeyOrNull(): String? {
        return when (this) {
            is Fixed -> additionalRequestKey
            is FixedWithNoMoreAdditionalData -> null
            is Loading -> additionalRequestKey
            is Error -> additionalRequestKey
        }
    }
}
