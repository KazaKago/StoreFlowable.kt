package com.kazakago.storeflowable.core

/**
 * Combine multiple [State].
 *
 * @param state2 The second [State] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [State] containing the combined data.
 */
fun <A, B, Z> State<A>.zip(state2: State<B>, transform: (rawContent1: A, rawContent2: B) -> Z): State<Z> {
    return when (this) {
        is State.Completed -> when (state2) {
            is State.Loading -> State.Loading(if (state2.content != null) transform(content, state2.content) else null)
            is State.Completed -> State.Completed(transform(content, state2.content))
            is State.Error -> State.Error(state2.exception)
        }
        is State.Loading -> when (state2) {
            is State.Loading -> State.Loading(null)
            is State.Completed -> State.Loading(null)
            is State.Error -> State.Error(state2.exception)
        }
        is State.Error -> when (state2) {
            is State.Loading -> State.Error(exception)
            is State.Completed -> State.Error(exception)
            is State.Error -> State.Error(exception)
        }
    }
}

/**
 * Combine multiple [State].
 *
 * @param state2 The second [State] to combine.
 * @param state3 The third [State] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [State] containing the combined data.
 */
fun <A, B, C, Z> State<A>.zip(state2: State<B>, state3: State<C>, transform: (rawContent1: A, rawContent2: B, rawContent3: C) -> Z): State<Z> {
    return zip(state2) { rawContent, other ->
        rawContent U other
    }.zip(state3) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, other)
    }
}

/**
 * Combine multiple [State].
 *
 * @param state2 The second [State] to combine.
 * @param state3 The third [State] to combine.
 * @param state4 The fourth [State] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [State] containing the combined data.
 */
fun <A, B, C, D, Z> State<A>.zip(state2: State<B>, state3: State<C>, state4: State<D>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D) -> Z): State<Z> {
    return zip(state2) { rawContent, other ->
        rawContent U other
    }.zip(state3) { rawContent, other ->
        rawContent U other
    }.zip(state4) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, rawContent.t2, other)
    }
}

/**
 * Combine multiple [State].
 *
 * @param state2 The second [State] to combine.
 * @param state3 The third [State] to combine.
 * @param state4 The fourth [State] to combine.
 * @param state5 The fifth [State] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [State] containing the combined data.
 */
fun <A, B, C, D, E, Z> State<A>.zip(state2: State<B>, state3: State<C>, state4: State<D>, state5: State<E>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D, rawContent5: E) -> Z): State<Z> {
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
