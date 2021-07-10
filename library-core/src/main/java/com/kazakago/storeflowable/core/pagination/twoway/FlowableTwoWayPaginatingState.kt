package com.kazakago.storeflowable.core.pagination.twoway

import kotlinx.coroutines.flow.Flow

/**
 * Type alias of `Flow<TwoWayPaginatingState<T>>`.
 */
typealias FlowableTwoWayPaginatingState<T> = Flow<TwoWayPaginatingState<T>>
