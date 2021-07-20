package com.kazakago.storeflowable.core

internal infix fun <R, T> R.U(other: T) = Tuple2(this, other)

internal data class Tuple2<T0, T1>(val t0: T0, val t1: T1) {

    infix fun <T> U(other: T) = Tuple3(t0, t1, other)

    fun toList() = listOf(t0, t1)
}

internal fun <T0, T1> tupleOf(t0: T0, t1: T1) = Tuple2(t0, t1)

internal fun <T> Tuple2<T, T>.toTypedList() = listOf(t0, t1)

internal data class Tuple3<T0, T1, T2>(val t0: T0, val t1: T1, val t2: T2) {

    infix fun <T> U(other: T) = Tuple4(t0, t1, t2, other)

    fun toList() = listOf(t0, t1, t2)
}

internal fun <T0, T1, T2> tupleOf(t0: T0, t1: T1, t2: T2) = Tuple3(t0, t1, t2)

internal fun <T> Tuple3<T, T, T>.toTypedList() = listOf(t0, t1, t2)

internal data class Tuple4<T0, T1, T2, T3>(val t0: T0, val t1: T1, val t2: T2, val t3: T3) {

    fun toList() = listOf(t0, t1, t2, t3)
}

internal fun <T0, T1, T2, T3> tupleOf(t0: T0, t1: T1, t2: T2, t3: T3) = Tuple4(t0, t1, t2, t3)

internal fun <T> Tuple4<T, T, T, T>.toTypedList() = listOf(t0, t1, t2, t3)
