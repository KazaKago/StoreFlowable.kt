package com.kazakago.storeflowable.example.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubOrg(
    @SerialName("id")
    val id: Long,
    @SerialName("login")
    val name: String,
)
