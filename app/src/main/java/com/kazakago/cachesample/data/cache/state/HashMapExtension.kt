package com.kazakago.cachesample.data.cache.state

import java.util.*

fun HashMap<String, Calendar>.getOrCreate(key: String): Calendar {
    return getOrPut(key, { Calendar.getInstance().apply { time = Date(0) } })
}
