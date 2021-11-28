package com.kazakago.storeflowable.example.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubMeta(
    @SerialName("verifiable_password_authentication")
    val verifiablePasswordAuthentication: Boolean,
    @SerialName("ssh_key_fingerprints")
    val sshKeyFingerprints: SshKeyFingerprints,
) {
    @Serializable
    data class SshKeyFingerprints(
        @SerialName("SHA256_RSA")
        val sha256Rsa: String,
        @SerialName("SHA256_ECDSA")
        val sha256Dsa: String,
    )
}
