package com.kazakago.storeflowable.datastate

/**
 * Indicates the state of the data.
 *
 * This state is only used inside this library.
 */
@Suppress("CanSealedSubClassBeObject")
sealed class DataState {
    internal class Fixed(val nextDataState: AdditionalDataState, val prevDataState: AdditionalDataState, val isInitial: Boolean = false) : DataState()
    internal class Loading : DataState()
    internal class Error(val exception: Exception) : DataState()

    internal fun nextDataStateOrNull(): AdditionalDataState {
        return when (this) {
            is Fixed -> nextDataState
            is Loading -> AdditionalDataState.FixedWithNoMoreAdditionalData()
            is Error -> AdditionalDataState.FixedWithNoMoreAdditionalData()
        }
    }

    internal fun prevDataStateOrNull(): AdditionalDataState {
        return when (this) {
            is Fixed -> prevDataState
            is Loading -> AdditionalDataState.FixedWithNoMoreAdditionalData()
            is Error -> AdditionalDataState.FixedWithNoMoreAdditionalData()
        }
    }

    internal fun nextKeyOrNull(): String? {
        return when (this) {
            is Fixed -> nextDataState.additionalRequestKeyOrNull()
            is Loading -> null
            is Error -> null
        }
    }

    internal fun prevKeyOrNull(): String? {
        return when (this) {
            is Fixed -> prevDataState.additionalRequestKeyOrNull()
            is Loading -> null
            is Error -> null
        }
    }
}
