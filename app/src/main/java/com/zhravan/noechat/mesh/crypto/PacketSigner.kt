package com.zhravan.noechat.mesh.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec

interface PacketSigner {
    val publicKeySpkiB64: String
    fun sign(payload: ByteArray): ByteArray
    fun verify(payload: ByteArray, signature: ByteArray, publicKeySpkiB64: String): Boolean
}

class KeystorePacketSigner(context: Context) : PacketSigner {

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    init {
        if (!keyStore.containsAlias(ALIAS)) {
            val generator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                ANDROID_KEYSTORE
            )
            val spec = KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_SIGN
            )
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setAlgorithmParameterSpec(ECGenParameterSpec(SECP256R1))
                .build()
            generator.initialize(spec)
            generator.generateKeyPair()
        }
    }

    override val publicKeySpkiB64: String
        get() {
            val cert = keyStore.getCertificate(ALIAS)
                ?: error("Signing certificate missing")
            return Base64.encodeToString(cert.publicKey.encoded, Base64.NO_WRAP)
        }

    override fun sign(payload: ByteArray): ByteArray {
        val entry = keyStore.getEntry(ALIAS, null) as KeyStore.PrivateKeyEntry
        val signature = Signature.getInstance(SHA256_ECDSA)
        signature.initSign(entry.privateKey)
        signature.update(payload)
        return signature.sign()
    }

    override fun verify(payload: ByteArray, signature: ByteArray, publicKeySpkiB64: String): Boolean {
        return runCatching {
            val decoded = Base64.decode(publicKeySpkiB64, Base64.NO_WRAP)
            val publicKey = KeyFactory.getInstance(EC).generatePublic(X509EncodedKeySpec(decoded))
            val sig = Signature.getInstance(SHA256_ECDSA)
            sig.initVerify(publicKey)
            sig.update(payload)
            sig.verify(signature)
        }.getOrDefault(false)
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "noechat_packet_sign"
        const val SECP256R1 = "secp256r1"
        const val SHA256_ECDSA = "SHA256withECDSA"
        const val EC = "EC"
    }
}
