package com.kazakago.storeflowable.core.pagination.twoway

import kotlinx.coroutines.flow.map

/**
 * Use when mapping raw data in [TwoWayLoadingState].
 *
 * @param transform This callback that returns the result of transforming the data.
 * @return Return [TwoWayLoadingState] containing the transformed data.
 */
fun <A, Z> FlowableTwoWayLoadingState<A>.mapContent(transform: suspend (content: A) -> Z): FlowableTwoWayLoadingState<Z> {
    return map {
        when (it) {
            is TwoWayLoadingState.Loading -> TwoWayLoadingState.Loading(if (it.content != null) transform(it.content) else null)
            is TwoWayLoadingState.Completed -> TwoWayLoadingState.Completed(transform(it.content), it.next, it.prev)
            is TwoWayLoadingState.Error -> TwoWayLoadingState.Error(it.exception)
        }
    }
}
