package com.kazakago.storeflowable.core

import kotlinx.coroutines.flow.combine

/**
 * Combine multiple [FlowableLoadingState].
 *
 * @param flowableState2 The second [FlowableLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableLoadingState] containing the combined data.
 */
fun <A, B, Z> FlowableLoadingState<A>.combineState(flowableState2: FlowableLoadingState<B>, transform: (rawContent1: A, rawContent2: B) -> Z): FlowableLoadingState<Z> {
    return combine(flowableState2) { state1, state2 ->
        state1.zip(state2) { rawContent, other ->
            transform(rawContent, other)
        }
    }
}

/**
 * Combine multiple [FlowableLoadingState].
 *
 * @param flowableState2 The second [FlowableLoadingState] to combine.
 * @param flowableState3 The third [FlowableLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableLoadingState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, Z> FlowableLoadingState<A>.combineState(flowableState2: FlowableLoadingState<B>, flowableState3: FlowableLoadingState<C>, transform: (rawContent1: A, rawContent2: B, rawContent3: C) -> Z): FlowableLoadingState<Z> {
    return combineState(flowableState2) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState3) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, other)
    }
}

/**
 * Combine multiple [FlowableLoadingState].
 *
 * @param flowableState2 The second [FlowableLoadingState] to combine.
 * @param flowableState3 The third [FlowableLoadingState] to combine.
 * @param flowableState4 The fourth [FlowableLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableLoadingState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, D, Z> FlowableLoadingState<A>.combineState(flowableState2: FlowableLoadingState<B>, flowableState3: FlowableLoadingState<C>, flowableState4: FlowableLoadingState<D>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D) -> Z): FlowableLoadingState<Z> {
    return combineState(flowableState2) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState3) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState4) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, rawContent.t2, other)
    }
}

/**
 * Combine multiple [FlowableLoadingState].
 *
 * @param flowableState2 The second [FlowableLoadingState] to combine.
 * @param flowableState3 The third [FlowableLoadingState] to combine.
 * @param flowableState4 The fourth [FlowableLoadingState] to combine.
 * @param flowableState5 The fifth [FlowableLoadingState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableLoadingState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, D, E, Z> FlowableLoadingState<A>.combineState(flowableState2: FlowableLoadingState<B>, flowableState3: FlowableLoadingState<C>, flowableState4: FlowableLoadingState<D>, flowableState5: FlowableLoadingState<E>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D, rawContent5: E) -> Z): FlowableLoadingState<Z> {
    return combineState(flowableState2) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState3) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState4) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState5) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, rawContent.t2, rawContent.t3, other)
    }
}
