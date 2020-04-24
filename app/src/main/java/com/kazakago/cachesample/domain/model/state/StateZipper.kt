package com.kazakago.cachesample.domain.model.state

import com.os.operando.guild.kt.to

fun <A, B, Z> State<A>.zip(otherState: State<B>, transform: (content1: A, content2: B) -> Z): State<Z> {
    return zipImpl(otherState, transform)
}

fun <A, B, C, Z> State<A>.zip(otherState1: State<B>, otherState2: State<C>, transform: (content1: A, content2: B, content3: C) -> Z): State<Z> {
    return zipImpl(otherState1) { content1, content2 ->
        content1 to content2
    }.zipImpl(otherState2) { content1_2, content3 ->
        transform(content1_2.first, content1_2.second, content3)
    }
}

fun <A, B, C, D, Z> State<A>.zip(otherState1: State<B>, otherState2: State<C>, otherState3: State<D>, transform: (content1: A, content2: B, content3: C, content4: D) -> Z): State<Z> {
    return zipImpl(otherState1) { content1, content2 ->
        content1 to content2
    }.zipImpl(otherState2) { content1_2, content3 ->
        content1_2.first to content1_2.second to content3
    }.zipImpl(otherState3) { content1_2_3, content4 ->
        transform(content1_2_3.first, content1_2_3.second, content1_2_3.third, content4)
    }
}

fun <A, B, C, D, E, Z> State<A>.zip(otherState1: State<B>, otherState2: State<C>, otherState3: State<D>, otherState4: State<E>, transform: (content1: A, content2: B, content3: C, content4: D, content5: E) -> Z): State<Z> {
    return zipImpl(otherState1) { content1, content2 ->
        content1 to content2
    }.zipImpl(otherState2) { content1_2, content3 ->
        content1_2.first to content1_2.second to content3
    }.zipImpl(otherState3) { content1_2_3, content4 ->
        content1_2_3.first to content1_2_3.second to content1_2_3.third to content4
    }.zipImpl(otherState4) { content1_2_3_4, content5 ->
        transform(content1_2_3_4.first, content1_2_3_4.second, content1_2_3_4.third, content1_2_3_4.fourth, content5)
    }
}

private fun <A, B, Z> State<A>.zipImpl(state: State<B>, transform: (content1: A, content2: B) -> Z): State<Z> {
    return when (this) {
        is State.Fixed -> this.zipImpl(state, transform)
        is State.Loading -> this.zipImpl(state, transform)
        is State.Error -> this.zipImpl(state, transform)
    }
}

private fun <A, B, Z> State.Fixed<A>.zipImpl(otherState: State<B>, transform: (content1: A, content2: B) -> Z): State<Z> {
    return when (otherState) {
        is State.Fixed -> State.Fixed(content.zip(otherState.content, transform))
        is State.Loading -> State.Loading(content.zip(otherState.content, transform))
        is State.Error -> State.Error(content.zip(otherState.content, transform), otherState.exception)
    }
}

private fun <A, B, Z> State.Loading<A>.zipImpl(otherState: State<B>, transform: (content1: A, content2: B) -> Z): State<Z> {
    return when (otherState) {
        is State.Fixed -> State.Loading(content.zip(otherState.content, transform))
        is State.Loading -> State.Loading(content.zip(otherState.content, transform))
        is State.Error -> State.Error(content.zip(otherState.content, transform), otherState.exception)
    }
}

private fun <A, B, Z> State.Error<A>.zipImpl(otherState: State<B>, transform: (content1: A, content2: B) -> Z): State<Z> {
    return when (otherState) {
        is State.Fixed -> State.Error(content.zip(otherState.content, transform), this.exception)
        is State.Loading -> State.Error(content.zip(otherState.content, transform), this.exception)
        is State.Error -> State.Error(content.zip(otherState.content, transform), this.exception)
    }
}
