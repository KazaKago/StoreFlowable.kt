package com.kazakago.cachesample.data.cache

import java.util.*

internal object GithubCache {
    var reposCache: HashMap<String, List<GithubRepoEntity>?> = hashMapOf()
    var reposCreateAdCache: HashMap<String, Calendar> = hashMapOf()
    var userCache: HashMap<String, GithubUserEntity?> = hashMapOf()
    var userCreateAdCache: HashMap<String, Calendar> = hashMapOf()
}
