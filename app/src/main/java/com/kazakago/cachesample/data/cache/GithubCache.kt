package com.kazakago.cachesample.data.cache

import com.kazakago.cachesample.data.cache.state.DataState
import com.kazakago.cachesample.data.cache.state.PagingDataState
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import java.util.*

object GithubCache {
    var reposCache: HashMap<String, List<GithubRepoEntity>?> = hashMapOf()
    var reposCreateAdCache: HashMap<String, Calendar> = hashMapOf()
    val reposState: HashMap<String, ConflatedBroadcastChannel<PagingDataState>> = hashMapOf()

    var userCache: HashMap<String, GithubUserEntity?> = hashMapOf()
    var userCreateAdCache: HashMap<String, Calendar> = hashMapOf()
    val userState: HashMap<String, ConflatedBroadcastChannel<DataState>> = hashMapOf()
}

