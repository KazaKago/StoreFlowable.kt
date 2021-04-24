package com.kazakago.storeflowable

/**
 * Indicates the state of the data.
 *
 * This state is only used inside the [StoreFlowable].
 */
sealed class DataState {

    /**
     * No processing state.
     *
     * @property noMoreAdditionalData Set to `true` if you know at Pagination that there is no more additional data. Has no effect without Pagination.
     */
    class Fixed(val noMoreAdditionalData: Boolean = false) : DataState()

    /**
     * Acquiring data state.
     */
    class Loading : DataState()

    /**
     * An error when processing state.
     *
     * @property exception The entity of the exception that occurred.
     */
    class Error(val exception: Exception) : DataState()
}
