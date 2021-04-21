package com.kazakago.storeflowable.core

import com.os.operando.guild.kt.to

/**
 * Combine multiple [StateContent].
 *
 * @param otherContent The second [StateContent] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [StateContent] containing the combined data.
 */
fun <A, B, Z> StateContent<A>.zip(otherContent: StateContent<B>, transform: (content1: A, content2: B) -> Z): StateContent<Z> {
    return when (this) {
        is StateContent.Exist -> this.zip(otherContent, transform)
        is StateContent.NotExist -> this.zip(otherContent)
    }
}

/**
 * Combine multiple [StateContent].
 *
 * @param otherContent1 The second [StateContent] to combine.
 * @param otherContent2 The third [StateContent] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [StateContent] containing the combined data.
 */
fun <A, B, C, Z> StateContent<A>.zip(otherContent1: StateContent<B>, otherContent2: StateContent<C>, transform: (content1: A, content2: B, content3: C) -> Z): StateContent<Z> {
    return zip(otherContent1) { content1, content2 ->
        content1 to content2
    }.zip(otherContent2) { content1_2, content3 ->
        transform(content1_2.first, content1_2.second, content3)
    }
}

/**
 * Combine multiple [StateContent].
 *
 * @param otherContent1 The second [StateContent] to combine.
 * @param otherContent2 The third [StateContent] to combine.
 * @param otherContent3 The fourth [StateContent] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [StateContent] containing the combined data.
 */
fun <A, B, C, D, Z> StateContent<A>.zip(otherContent1: StateContent<B>, otherContent2: StateContent<C>, otherContent3: StateContent<D>, transform: (content1: A, content2: B, content3: C, content4: D) -> Z): StateContent<Z> {
    return zip(otherContent1) { content1, content2 ->
        content1 to content2
    }.zip(otherContent2) { content1_2, content3 ->
        content1_2.first to content1_2.second to content3
    }.zip(otherContent3) { content1_2_3, content4 ->
        transform(content1_2_3.first, content1_2_3.second, content1_2_3.third, content4)
    }
}

/**
 * Combine multiple [StateContent].
 *
 * @param otherContent1 The second [StateContent] to combine.
 * @param otherContent2 The third [StateContent] to combine.
 * @param otherContent3 The fourth [StateContent] to combine.
 * @param otherContent4 The fifth [StateContent] to combine.
 * @param transform This callback that returns the result of combining the data.
 * @return Return [StateContent] containing the combined data.
 */
fun <A, B, C, D, E, Z> StateContent<A>.zip(otherContent1: StateContent<B>, otherContent2: StateContent<C>, otherContent3: StateContent<D>, otherContent4: StateContent<E>, transform: (content1: A, content2: B, content3: C, content4: D, content5: E) -> Z): StateContent<Z> {
    return zip(otherContent1) { content1, content2 ->
        content1 to content2
    }.zip(otherContent2) { content1_2, content3 ->
        content1_2.first to content1_2.second to content3
    }.zip(otherContent3) { content1_2_3, content4 ->
        content1_2_3.first to content1_2_3.second to content1_2_3.third to content4
    }.zip(otherContent4) { content1_2_3_4, content5 ->
        transform(content1_2_3_4.first, content1_2_3_4.second, content1_2_3_4.third, content1_2_3_4.fourth, content5)
    }
}

private fun <A, B, Z> StateContent.Exist<A>.zip(otherContent: StateContent<B>, transform: (content1: A, content2: B) -> Z): StateContent<Z> {
    return when (otherContent) {
        is StateContent.Exist -> StateContent.Exist(transform(rawContent, otherContent.rawContent))
        is StateContent.NotExist -> StateContent.NotExist()
    }
}

private fun <A, B, Z> StateContent.NotExist<A>.zip(otherContent: StateContent<B>): StateContent<Z> {
    return when (otherContent) {
        is StateContent.Exist -> StateContent.NotExist()
        is StateContent.NotExist -> StateContent.NotExist()
    }
}
