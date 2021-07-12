package com.kazakago.storeflowable.core.pagination.oneway

import com.kazakago.storeflowable.core.U
import com.kazakago.storeflowable.core.pagination.zip

/**
 * Combine multiple [OneWayLoadingState].
 *
 * @param state2 The second [OneWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [OneWayLoadingState] containing the combined data.
 */
fun <A, B, Z> OneWayLoadingState<A>.zip(state2: OneWayLoadingState<B>, transform: (rawContent1: A, rawContent2: B) -> Z): OneWayLoadingState<Z> {
    return when (this) {
        is OneWayLoadingState.Completed -> when (state2) {
            is OneWayLoadingState.Loading -> OneWayLoadingState.Loading(if (state2.content != null) transform(content, state2.content) else null)
            is OneWayLoadingState.Completed -> OneWayLoadingState.Completed(transform(content, state2.content), appending.zip(state2.appending))
            is OneWayLoadingState.Error -> OneWayLoadingState.Error(state2.exception)
        }
        is OneWayLoadingState.Loading -> when (state2) {
            is OneWayLoadingState.Loading -> OneWayLoadingState.Loading(if (content != null && state2.content != null) transform(content, state2.content) else null)
            is OneWayLoadingState.Completed -> OneWayLoadingState.Loading(if (content != null) transform(content, state2.content) else null)
            is OneWayLoadingState.Error -> OneWayLoadingState.Error(state2.exception)
        }
        is OneWayLoadingState.Error -> when (state2) {
            is OneWayLoadingState.Loading -> OneWayLoadingState.Error(exception)
            is OneWayLoadingState.Completed -> OneWayLoadingState.Error(exception)
            is OneWayLoadingState.Error -> OneWayLoadingState.Error(exception)
        }
    }
}

/**
 * Combine multiple [OneWayLoadingState].
 *
 * @param state2 The second [OneWayLoadingState] to combine.
 * @param state3 The third [OneWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [OneWayLoadingState] containing the combined data.
 */
fun <A, B, C, Z> OneWayLoadingState<A>.zip(state2: OneWayLoadingState<B>, state3: OneWayLoadingState<C>, transform: (rawContent1: A, rawContent2: B, rawContent3: C) -> Z): OneWayLoadingState<Z> {
    return zip(state2) { rawContent, other ->
        rawContent U other
    }.zip(state3) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, other)
    }
}

/**
 * Combine multiple [OneWayLoadingState].
 *
 * @param state2 The second [OneWayLoadingState] to combine.
 * @param state3 The third [OneWayLoadingState] to combine.
 * @param state4 The fourth [OneWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [OneWayLoadingState] containing the combined data.
 */
fun <A, B, C, D, Z> OneWayLoadingState<A>.zip(state2: OneWayLoadingState<B>, state3: OneWayLoadingState<C>, state4: OneWayLoadingState<D>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D) -> Z): OneWayLoadingState<Z> {
    return zip(state2) { rawContent, other ->
        rawContent U other
    }.zip(state3) { rawContent, other ->
        rawContent U other
    }.zip(state4) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, rawContent.t2, other)
    }
}

/**
 * Combine multiple [OneWayLoadingState].
 *
 * @param state2 The second [OneWayLoadingState] to combine.
 * @param state3 The third [OneWayLoadingState] to combine.
 * @param state4 The fourth [OneWayLoadingState] to combine.
 * @param state5 The fifth [OneWayLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [OneWayLoadingState] containing the combined data.
 */
fun <A, B, C, D, E, Z> OneWayLoadingState<A>.zip(state2: OneWayLoadingState<B>, state3: OneWayLoadingState<C>, state4: OneWayLoadingState<D>, state5: OneWayLoadingState<E>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D, rawContent5: E) -> Z): OneWayLoadingState<Z> {
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
