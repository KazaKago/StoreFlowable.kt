package com.kazakago.storeflowable

/**
 * Indicates the state of the data.
 *
 * This state is only used inside the [StoreFlowable].
 */
sealed class DataState {

    /**
     * No processing state.
     */
    class Fixed(val noMoreAdditionalData: Boolean = false) : DataState()

    /**
     * Acquiring data state.
     */
    class Loading : DataState()

    /**
     * An error when processing state.
     */
    class Error(val exception: Exception) : DataState()
}
