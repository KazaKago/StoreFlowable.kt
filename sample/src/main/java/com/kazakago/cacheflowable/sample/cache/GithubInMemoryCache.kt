package com.kazakago.cacheflowable.sample.cache

import com.kazakago.cacheflowable.sample.model.GithubMeta
import com.kazakago.cacheflowable.sample.model.GithubOrg
import com.kazakago.cacheflowable.sample.model.GithubRepo
import com.kazakago.cacheflowable.sample.model.GithubUser
import java.util.*

object GithubInMemoryCache {
    var metaCache: GithubMeta? = null
    var metaCreatedAtCache: Calendar? = null

    var orgsCache: List<GithubOrg>? = null
    var orgsCreatedAtCache: Calendar? = null

    var userCache: HashMap<String, GithubUser?> = hashMapOf()
    var userCreateAdCache: HashMap<String, Calendar> = hashMapOf()

    var reposCache: HashMap<String, List<GithubRepo>?> = hashMapOf()
    var reposCreateAdCache: HashMap<String, Calendar> = hashMapOf()
}
