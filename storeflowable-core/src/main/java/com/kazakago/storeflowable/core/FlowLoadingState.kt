package com.kazakago.storeflowable.core

import kotlinx.coroutines.flow.Flow

/**
 * Type alias of `Flow<LoadingState<T>>`.
 */
typealias FlowLoadingState<T> = Flow<LoadingState<T>>
