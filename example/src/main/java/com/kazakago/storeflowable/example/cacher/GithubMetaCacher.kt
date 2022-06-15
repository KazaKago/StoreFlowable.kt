package com.kazakago.storeflowable.example.cacher

import com.kazakago.storeflowable.cacher.Cacher
import com.kazakago.storeflowable.example.model.GithubMeta
import kotlin.time.Duration.Companion.minutes

object GithubMetaCacher : Cacher<Unit, GithubMeta>() {
    override val expireSeconds = 1.minutes.inWholeSeconds
}
