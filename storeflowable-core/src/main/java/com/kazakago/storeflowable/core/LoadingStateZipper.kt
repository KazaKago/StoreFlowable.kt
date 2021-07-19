package com.kazakago.storeflowable.core

/**
 * Combine multiple [LoadingState].
 *
 * @param state2 The second [LoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [LoadingState] containing the combined data.
 */
fun <A, B, Z> LoadingState<A>.zip(state2: LoadingState<B>, transform: (rawContent1: A, rawContent2: B) -> Z): LoadingState<Z> {
    return when (this) {
        is LoadingState.Completed -> when (state2) {
            is LoadingState.Loading -> LoadingState.Loading(if (state2.content != null) transform(content, state2.content) else null)
            is LoadingState.Completed -> LoadingState.Completed(transform(content, state2.content), next.zip(state2.next), prev.zip(state2.prev))
            is LoadingState.Error -> LoadingState.Error(state2.exception)
        }
        is LoadingState.Loading -> when (state2) {
            is LoadingState.Loading -> LoadingState.Loading(if (content != null && state2.content != null) transform(content, state2.content) else null)
            is LoadingState.Completed -> LoadingState.Loading(if (content != null) transform(content, state2.content) else null)
            is LoadingState.Error -> LoadingState.Error(state2.exception)
        }
        is LoadingState.Error -> when (state2) {
            is LoadingState.Loading -> LoadingState.Error(exception)
            is LoadingState.Completed -> LoadingState.Error(exception)
            is LoadingState.Error -> LoadingState.Error(exception)
        }
    }
}

/**
 * Combine multiple [LoadingState].
 *
 * @param state2 The second [LoadingState] to combine.
 * @param state3 The third [LoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [LoadingState] containing the combined data.
 */
fun <A, B, C, Z> LoadingState<A>.zip(state2: LoadingState<B>, state3: LoadingState<C>, transform: (rawContent1: A, rawContent2: B, rawContent3: C) -> Z): LoadingState<Z> {
    return zip(state2) { rawContent, other ->
        rawContent U other
    }.zip(state3) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, other)
    }
}

/**
 * Combine multiple [LoadingState].
 *
 * @param state2 The second [LoadingState] to combine.
 * @param state3 The third [LoadingState] to combine.
 * @param state4 The fourth [LoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [LoadingState] containing the combined data.
 */
fun <A, B, C, D, Z> LoadingState<A>.zip(state2: LoadingState<B>, state3: LoadingState<C>, state4: LoadingState<D>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D) -> Z): LoadingState<Z> {
    return zip(state2) { rawContent, other ->
        rawContent U other
    }.zip(state3) { rawContent, other ->
        rawContent U other
    }.zip(state4) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, rawContent.t2, other)
    }
}

/**
 * Combine multiple [LoadingState].
 *
 * @param state2 The second [LoadingState] to combine.
 * @param state3 The third [LoadingState] to combine.
 * @param state4 The fourth [LoadingState] to combine.
 * @param state5 The fifth [LoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [LoadingState] containing the combined data.
 */
fun <A, B, C, D, E, Z> LoadingState<A>.zip(state2: LoadingState<B>, state3: LoadingState<C>, state4: LoadingState<D>, state5: LoadingState<E>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D, rawContent5: E) -> Z): LoadingState<Z> {
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
