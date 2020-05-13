package com.kazakago.cachesample.data.cache

import com.kazakago.cachesample.data.cache.state.DataState
import com.kazakago.cachesample.data.cache.state.PagingDataState
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

object DefaultDataStateCache {
    val pagingDataState: HashMap<String, MutableStateFlow<PagingDataState>> = hashMapOf()
    val dataState: HashMap<String, MutableStateFlow<DataState>> = hashMapOf()
}
