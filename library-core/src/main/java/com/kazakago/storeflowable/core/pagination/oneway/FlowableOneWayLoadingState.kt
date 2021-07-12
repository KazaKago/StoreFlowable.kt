package com.kazakago.storeflowable.core.pagination.oneway

import kotlinx.coroutines.flow.Flow

/**
 * Type alias of `Flow<OneWayLoadingState<T>>`.
 */
typealias FlowableOneWayLoadingState<T> = Flow<OneWayLoadingState<T>>
