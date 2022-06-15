package com.kazakago.storeflowable.example.cacher

import com.kazakago.storeflowable.cacher.TwoWayPaginationCacher
import com.kazakago.storeflowable.example.model.GithubRepo
import kotlin.time.Duration.Companion.minutes

object GithubTwoWayReposCacher : TwoWayPaginationCacher<Unit, GithubRepo>() {
    override val expireSeconds = 1.minutes.inWholeSeconds
}
