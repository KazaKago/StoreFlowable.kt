package com.kazakago.storeflowable.core

import kotlinx.coroutines.flow.combine

/**
 * Combine multiple [FlowableState].
 *
 * @param flowableState2 The second [FlowableState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableState] containing the combined data.
 */
fun <A, B, Z> FlowableState<A>.combineState(flowableState2: FlowableState<B>, transform: (rawContent1: A, rawContent2: B) -> Z): FlowableState<Z> {
    return combine(flowableState2) { state1, state2 ->
        state1.zip(state2) { rawContent, other ->
            transform(rawContent, other)
        }
    }
}

/**
 * Combine multiple [FlowableState].
 *
 * @param flowableState2 The second [FlowableState] to combine.
 * @param flowableState3 The third [FlowableState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, Z> FlowableState<A>.combineState(flowableState2: FlowableState<B>, flowableState3: FlowableState<C>, transform: (rawContent1: A, rawContent2: B, rawContent3: C) -> Z): FlowableState<Z> {
    return combineState(flowableState2) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState3) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, other)
    }
}

/**
 * Combine multiple [FlowableState].
 *
 * @param flowableState2 The second [FlowableState] to combine.
 * @param flowableState3 The third [FlowableState] to combine.
 * @param flowableState4 The fourth [FlowableState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, D, Z> FlowableState<A>.combineState(flowableState2: FlowableState<B>, flowableState3: FlowableState<C>, flowableState4: FlowableState<D>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D) -> Z): FlowableState<Z> {
    return combineState(flowableState2) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState3) { rawContent, other ->
        rawContent U other
    }.combineState(flowableState4) { rawContent, other ->
        transform(rawContent.t0, rawContent.t1, rawContent.t2, other)
    }
}

/**
 * Combine multiple [FlowableState].
 *
 * @param flowableState2 The second [FlowableState] to combine.
 * @param flowableState3 The third [FlowableState] to combine.
 * @param flowableState4 The fourth [FlowableState] to combine.
 * @param flowableState5 The fifth [FlowableState] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [FlowableState] containing the combined data.
 * @see combineState
 */
fun <A, B, C, D, E, Z> FlowableState<A>.combineState(flowableState2: FlowableState<B>, flowableState3: FlowableState<C>, flowableState4: FlowableState<D>, flowableState5: FlowableState<E>, transform: (rawContent1: A, rawContent2: B, rawContent3: C, rawContent4: D, rawContent5: E) -> Z): FlowableState<Z> {
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
