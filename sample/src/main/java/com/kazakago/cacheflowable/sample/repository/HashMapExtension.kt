package com.kazakago.cacheflowable.sample.repository

import java.util.*

fun HashMap<String, Calendar>.getOrCreate(key: String): Calendar {
    return getOrPut(key, { Calendar.getInstance().apply { time = Date(0) } })
}
