package com.kazakago.storeflowable.core

import kotlinx.coroutines.flow.combine

/**
 * Combine multiple [FlowLoadingState].
 *
 * @param flowState2 The second [FlowLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowLoadingState] containing the combined data.
 */
fun <A, B, Z> FlowLoadingState<A>.combineState(flowState2: FlowLoadingState<B>, transform: (rawContent1: A, rawContent2: B) -> Z): FlowLoadingState<Z> {
    return combine(flowState2) { state1, state2 ->
        state1.zip(state2) { rawContent, other ->
            transform(rawContent, other)
        }
    }
}

/**
 * Combine multiple [FlowLoadingState].
 *
 * @param flowState2 The second [FlowLoadingState] to combine.
 * @param flowState3 The third [FlowLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowLoadingState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, Z> FlowLoadingState<A>.combineState(flowState2: FlowLoadingState<B>, flowState3: FlowLoadingState<C>, transform: (rawContent1: A, rawContent2: B, rawContent3: C) -> Z): FlowLoadingState<Z> {
    return combineState(flowState2) { rawContent, other ->
        rawContent U other
    }.combineState(flowState3) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, other)
    }
}

/**
 * Combine multiple [FlowLoadingState].
 *
 * @param flowState2 The second [FlowLoadingState] to combine.
 * @param flowState3 The third [FlowLoadingState] to combine.
 * @param flowState4 The fourth [FlowLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowLoadingState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, D, Z> FlowLoadingState<A>.combineState(flowState2: FlowLoadingState<B>, flowState3: FlowLoadingState<C>, flowState4: FlowLoadingState<D>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D) -> Z): FlowLoadingState<Z> {
    return combineState(flowState2) { rawContent, other ->
        rawContent U other
    }.combineState(flowState3) { rawContent, other ->
        rawContent U other
    }.combineState(flowState4) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, rawContent.t2, other)
    }
}

/**
 * Combine multiple [FlowLoadingState].
 *
 * @param flowState2 The second [FlowLoadingState] to combine.
 * @param flowState3 The third [FlowLoadingState] to combine.
 * @param flowState4 The fourth [FlowLoadingState] to combine.
 * @param flowState5 The fifth [FlowLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowLoadingState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, D, E, Z> FlowLoadingState<A>.combineState(flowState2: FlowLoadingState<B>, flowState3: FlowLoadingState<C>, flowState4: FlowLoadingState<D>, flowState5: FlowLoadingState<E>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D, rawContent5: E) -> Z): FlowLoadingState<Z> {
    return combineState(flowState2) { rawContent, other ->
        rawContent U other
    }.combineState(flowState3) { rawContent, other ->
        rawContent U other
    }.combineState(flowState4) { rawContent, other ->
        rawContent U other
    }.combineState(flowState5) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, rawContent.t2, rawContent.t3, other)
    }
}
