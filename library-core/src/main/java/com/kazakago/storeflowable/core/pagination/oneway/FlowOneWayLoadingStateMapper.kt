package com.kazakago.storeflowable.core.pagination.oneway

import kotlinx.coroutines.flow.map

/**
 * Use when mapping raw data in [FlowableOneWayLoadingState].
 *
 * @param transform This callback that returns the result of transforming the data.
 * @return Return [FlowableOneWayLoadingState] containing the transformed data.
 */
fun <A, Z> FlowableOneWayLoadingState<A>.mapContent(transform: suspend (content: A) -> Z): FlowableOneWayLoadingState<Z> {
    return map {
        when (it) {
            is OneWayLoadingState.Loading -> OneWayLoadingState.Loading(if (it.content != null) transform(it.content) else null)
            is OneWayLoadingState.Completed -> OneWayLoadingState.Completed(transform(it.content), it.appending)
            is OneWayLoadingState.Error -> OneWayLoadingState.Error(it.exception)
        }
    }
}
