package com.kazakago.storeflowable.sample.cache

import com.kazakago.storeflowable.sample.model.GithubMeta
import com.kazakago.storeflowable.sample.model.GithubOrg
import com.kazakago.storeflowable.sample.model.GithubRepo
import com.kazakago.storeflowable.sample.model.GithubUser
import java.time.LocalDateTime
import java.util.*

object GithubInMemoryCache {
    var metaCache: GithubMeta? = null
    var metaCacheCreatedAt: LocalDateTime? = null

    var orgsCache: List<GithubOrg>? = null
    var orgsCacheCreatedAt: LocalDateTime? = null

    var userCache: HashMap<String, GithubUser?> = hashMapOf()
    var userCacheCreateAt: HashMap<String, LocalDateTime> = hashMapOf()

    var reposCache: HashMap<String, List<GithubRepo>?> = hashMapOf()
    var reposCacheCreatedAt: HashMap<String, LocalDateTime> = hashMapOf()
}
