package com.kazakago.cachesample.data.cache

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import java.util.*

object GithubCache {
    var reposCache: HashMap<String, List<GithubRepoEntity>?> = hashMapOf()
    var reposCreateAdCache: HashMap<String, Calendar> = hashMapOf()
    val reposState: HashMap<String, ConflatedBroadcastChannel<PagingDataState>> = hashMapOf()
}

