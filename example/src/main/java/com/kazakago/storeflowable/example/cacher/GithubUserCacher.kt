package com.kazakago.storeflowable.example.cacher

import com.kazakago.storeflowable.cacher.Cacher
import com.kazakago.storeflowable.example.model.GithubUser
import kotlin.time.Duration.Companion.minutes

object GithubUserCacher : Cacher<String, GithubUser>() {
    override val expireSeconds = 30.minutes.inWholeSeconds
}
