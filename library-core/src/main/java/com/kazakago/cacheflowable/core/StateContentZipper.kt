package com.kazakago.cacheflowable.core

import com.os.operando.guild.kt.to

fun <A, B, Z> StateContent<A>.zip(otherStateContent: StateContent<B>, transform: (content1: A, content2: B) -> Z): StateContent<Z> {
    return when (this) {
        is StateContent.Exist -> this.zip(otherStateContent, transform)
        is StateContent.NotExist -> this.zip(otherStateContent)
    }
}

fun <A, B, C, Z> StateContent<A>.zip(otherStateContent1: StateContent<B>, otherStateContent2: StateContent<C>, transform: (content1: A, content2: B, content3: C) -> Z): StateContent<Z> {
    return zip(otherStateContent1) { content1, content2 ->
        content1 to content2
    }.zip(otherStateContent2) { content1_2, content3 ->
        transform(content1_2.first, content1_2.second, content3)
    }
}

fun <A, B, C, D, Z> StateContent<A>.zip(otherStateContent1: StateContent<B>, otherStateContent2: StateContent<C>, otherStateContent3: StateContent<D>, transform: (content1: A, content2: B, content3: C, content4: D) -> Z): StateContent<Z> {
    return zip(otherStateContent1) { content1, content2 ->
        content1 to content2
    }.zip(otherStateContent2) { content1_2, content3 ->
        content1_2.first to content1_2.second to content3
    }.zip(otherStateContent3) { content1_2_3, content4 ->
        transform(content1_2_3.first, content1_2_3.second, content1_2_3.third, content4)
    }
}

fun <A, B, C, D, E, Z> StateContent<A>.zip(otherStateContent1: StateContent<B>, otherStateContent2: StateContent<C>, otherStateContent3: StateContent<D>, otherStateContent4: StateContent<E>, transform: (content1: A, content2: B, content3: C, content4: D, content5: E) -> Z): StateContent<Z> {
    return zip(otherStateContent1) { content1, content2 ->
        content1 to content2
    }.zip(otherStateContent2) { content1_2, content3 ->
        content1_2.first to content1_2.second to content3
    }.zip(otherStateContent3) { content1_2_3, content4 ->
        content1_2_3.first to content1_2_3.second to content1_2_3.third to content4
    }.zip(otherStateContent4) { content1_2_3_4, content5 ->
        transform(content1_2_3_4.first, content1_2_3_4.second, content1_2_3_4.third, content1_2_3_4.fourth, content5)
    }
}

private fun <A, B, Z> StateContent.Exist<A>.zip(otherStateContent: StateContent<B>, transform: (content1: A, content2: B) -> Z): StateContent<Z> {
    return when (otherStateContent) {
        is StateContent.Exist -> StateContent.Exist(transform(rawContent, otherStateContent.rawContent))
        is StateContent.NotExist -> StateContent.NotExist()
    }
}

private fun <A, B, Z> StateContent.NotExist<A>.zip(otherStateContent: StateContent<B>): StateContent<Z> {
    return when (otherStateContent) {
        is StateContent.Exist -> StateContent.NotExist()
        is StateContent.NotExist -> StateContent.NotExist()
    }
}
