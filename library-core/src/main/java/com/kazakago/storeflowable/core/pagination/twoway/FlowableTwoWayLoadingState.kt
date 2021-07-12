package com.kazakago.storeflowable.core.pagination.twoway

import kotlinx.coroutines.flow.Flow

/**
 * Type alias of `Flow<TwoWayLoadingState<T>>`.
 */
typealias FlowableTwoWayLoadingState<T> = Flow<TwoWayLoadingState<T>>
