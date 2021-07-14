package com.kazakago.storeflowable.core.pagination.twoway

import com.kazakago.storeflowable.core.U
import com.kazakago.storeflowable.core.pagination.zip

/**
 * Combine multiple [TwoWayLoadingState].
 *
 * @param state2 The second [TwoWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [TwoWayLoadingState] containing the combined data.
 */
fun <A, B, Z> TwoWayLoadingState<A>.zip(state2: TwoWayLoadingState<B>, transform: (rawContent1: A, rawContent2: B) -> Z): TwoWayLoadingState<Z> {
    return when (this) {
        is TwoWayLoadingState.Completed -> when (state2) {
            is TwoWayLoadingState.Loading -> TwoWayLoadingState.Loading(if (state2.content != null) transform(content, state2.content) else null)
            is TwoWayLoadingState.Completed -> TwoWayLoadingState.Completed(transform(content, state2.content), next.zip(state2.next), prev.zip(state2.prev))
            is TwoWayLoadingState.Error -> TwoWayLoadingState.Error(state2.exception)
        }
        is TwoWayLoadingState.Loading -> when (state2) {
            is TwoWayLoadingState.Loading -> TwoWayLoadingState.Loading(if (content != null && state2.content != null) transform(content, state2.content) else null)
            is TwoWayLoadingState.Completed -> TwoWayLoadingState.Loading(if (content != null) transform(content, state2.content) else null)
            is TwoWayLoadingState.Error -> TwoWayLoadingState.Error(state2.exception)
        }
        is TwoWayLoadingState.Error -> when (state2) {
            is TwoWayLoadingState.Loading -> TwoWayLoadingState.Error(exception)
            is TwoWayLoadingState.Completed -> TwoWayLoadingState.Error(exception)
            is TwoWayLoadingState.Error -> TwoWayLoadingState.Error(exception)
        }
    }
}

/**
 * Combine multiple [TwoWayLoadingState].
 *
 * @param state2 The second [TwoWayLoadingState] to combine.
 * @param state3 The third [TwoWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [TwoWayLoadingState] containing the combined data.
 */
fun <A, B, C, Z> TwoWayLoadingState<A>.zip(state2: TwoWayLoadingState<B>, state3: TwoWayLoadingState<C>, transform: (rawContent1: A, rawContent2: B, rawContent3: C) -> Z): TwoWayLoadingState<Z> {
    return zip(state2) { rawContent, other ->
        rawContent U other
    }.zip(state3) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, other)
    }
}

/**
 * Combine multiple [TwoWayLoadingState].
 *
 * @param state2 The second [TwoWayLoadingState] to combine.
 * @param state3 The third [TwoWayLoadingState] to combine.
 * @param state4 The fourth [TwoWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [TwoWayLoadingState] containing the combined data.
 */
fun <A, B, C, D, Z> TwoWayLoadingState<A>.zip(state2: TwoWayLoadingState<B>, state3: TwoWayLoadingState<C>, state4: TwoWayLoadingState<D>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D) -> Z): TwoWayLoadingState<Z> {
    return zip(state2) { rawContent, other ->
        rawContent U other
    }.zip(state3) { rawContent, other ->
        rawContent U other
    }.zip(state4) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, rawContent.t2, other)
    }
}

/**
 * Combine multiple [TwoWayLoadingState].
 *
 * @param state2 The second [TwoWayLoadingState] to combine.
 * @param state3 The third [TwoWayLoadingState] to combine.
 * @param state4 The fourth [TwoWayLoadingState] to combine.
 * @param state5 The fifth [TwoWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [TwoWayLoadingState] containing the combined data.
 */
fun <A, B, C, D, E, Z> TwoWayLoadingState<A>.zip(state2: TwoWayLoadingState<B>, state3: TwoWayLoadingState<C>, state4: TwoWayLoadingState<D>, state5: TwoWayLoadingState<E>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D, rawContent5: E) -> Z): TwoWayLoadingState<Z> {
    return zip(state2) { rawContent, other ->
        rawContent U other
    }.zip(state3) { rawContent, other ->
        rawContent U other
    }.zip(state4) { rawContent, other ->
        rawContent U other
    }.zip(state5) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, rawContent.t2, rawContent.t3, other)
    }
}
