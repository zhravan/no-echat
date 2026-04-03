package com.zhravan.noechat.mesh.wire

import android.util.Base64
import org.json.JSONObject

object WirePacketCodec {
    private const val KEY_V = "v"
    private const val KEY_ID = "id"
    private const val KEY_SID = "sid"
    private const val KEY_SPKI = "spki"
    private const val KEY_C = "c"
    private const val KEY_ST = "st"
    private const val KEY_N = "n"
    private const val KEY_LA = "la"
    private const val KEY_LO = "lo"
    private const val KEY_E = "e"
    private const val KEY_H = "h"
    private const val KEY_O = "o"
    private const val KEY_SIG = "sig"

    fun encode(packet: WirePacket): ByteArray {
        val json = JSONObject()
        json.put(KEY_V, packet.version)
        json.put(KEY_ID, packet.publicId)
        json.put(KEY_SID, packet.senderDeviceId)
        json.put(KEY_SPKI, packet.signingPublicKeySpkiB64)
        json.put(KEY_C, packet.createdAtEpochMs)
        json.put(KEY_ST, packet.status)
        packet.note?.let { json.put(KEY_N, it) } ?: json.put(KEY_N, JSONObject.NULL)
        packet.latitude?.let { json.put(KEY_LA, it) } ?: json.put(KEY_LA, JSONObject.NULL)
        packet.longitude?.let { json.put(KEY_LO, it) } ?: json.put(KEY_LO, JSONObject.NULL)
        json.put(KEY_E, packet.expiresAtEpochMs)
        json.put(KEY_H, packet.hopCount)
        json.put(KEY_O, packet.origin)
        json.put(KEY_SIG, packet.signatureB64)
        return json.toString().toByteArray(Charsets.UTF_8)
    }

    fun decode(bytes: ByteArray): WirePacket {
        val json = JSONObject(String(bytes, Charsets.UTF_8))
        return WirePacket(
            version = json.getInt(KEY_V),
            publicId = json.getString(KEY_ID),
            senderDeviceId = json.getString(KEY_SID),
            signingPublicKeySpkiB64 = json.getString(KEY_SPKI),
            createdAtEpochMs = json.getLong(KEY_C),
            status = json.getString(KEY_ST),
            note = if (json.isNull(KEY_N)) null else json.getString(KEY_N),
            latitude = if (json.isNull(KEY_LA)) null else json.getDouble(KEY_LA),
            longitude = if (json.isNull(KEY_LO)) null else json.getDouble(KEY_LO),
            expiresAtEpochMs = json.getLong(KEY_E),
            hopCount = json.getInt(KEY_H),
            origin = json.getString(KEY_O),
            signatureB64 = json.getString(KEY_SIG)
        )
    }

    fun decodeSignature(signatureB64: String): ByteArray =
        Base64.decode(signatureB64, Base64.NO_WRAP)
}
