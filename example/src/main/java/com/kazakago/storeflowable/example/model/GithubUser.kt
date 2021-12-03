package com.kazakago.storeflowable.example.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubUser(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("html_url")
    val htmlUrl: String,
    @SerialName("avatar_url")
    val avatarUrl: String
)
