package com.kazakago.storeflowable.example.cacher

import com.kazakago.storeflowable.cacher.PaginationCacher
import com.kazakago.storeflowable.example.model.GithubOrg
import kotlin.time.Duration.Companion.minutes

object GithubOrgsCacher : PaginationCacher<Unit, GithubOrg>() {
    override val expireTime = 30.minutes
}
