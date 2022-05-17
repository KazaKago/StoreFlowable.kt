package com.kazakago.storeflowable.example.cacher

import com.kazakago.storeflowable.cacher.PaginationCacher
import com.kazakago.storeflowable.example.model.GithubRepo
import kotlin.time.Duration.Companion.minutes

object GithubReposCacher : PaginationCacher<String, GithubRepo>() {
    override val expireSeconds = 30.minutes.inWholeSeconds
}
