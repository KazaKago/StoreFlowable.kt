package com.kazakago.cacheflowable.sample.cache

import com.kazakago.cacheflowable.sample.model.GithubRepo
import com.kazakago.cacheflowable.sample.model.GithubUser
import java.util.*

object GithubInMemoryCache {
    var reposCache: HashMap<String, List<GithubRepo>?> = hashMapOf()
    var reposCreateAdCache: HashMap<String, Calendar> = hashMapOf()
    var userCache: HashMap<String, GithubUser?> = hashMapOf()
    var userCreateAdCache: HashMap<String, Calendar> = hashMapOf()
}
