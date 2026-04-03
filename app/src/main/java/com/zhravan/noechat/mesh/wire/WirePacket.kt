package com.zhravan.noechat.mesh.wire

data class WirePacket(
    val version: Int,
    val publicId: String,
    val senderDeviceId: String,
    val signingPublicKeySpkiB64: String,
    val createdAtEpochMs: Long,
    val status: String,
    val note: String?,
    val latitude: Double?,
    val longitude: Double?,
    val expiresAtEpochMs: Long,
    val hopCount: Int,
    val origin: String,
    val signatureB64: String
) {
    fun bytesForSigning(): ByteArray {
        val n = note ?: ""
        val la = latitude?.toString() ?: ""
        val lo = longitude?.toString() ?: ""
        val parts = listOf(
            version.toString(),
            publicId,
            senderDeviceId,
            signingPublicKeySpkiB64,
            createdAtEpochMs.toString(),
            status,
            n,
            la,
            lo,
            expiresAtEpochMs.toString(),
            origin
        )
        return parts.joinToString("|").toByteArray(Charsets.UTF_8)
    }
}
