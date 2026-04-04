package com.zhravan.noechat.mesh

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

object GattChunkFramer {

    private val MAGIC = byteArrayOf(0x4E, 0x45)

    const val HEADER_BYTES: Int = 13

    fun looksLikeChunk(bytes: ByteArray): Boolean =
        bytes.size >= 2 && bytes[0] == MAGIC[0] && bytes[1] == MAGIC[1]

    fun split(payload: ByteArray, maxBody: Int, msgId: Int): List<ByteArray> {
        require(maxBody >= 16) { "maxBody too small" }
        if (payload.isEmpty()) return emptyList()
        val bodies = ArrayList<ByteArray>()
        var offset = 0
        while (offset < payload.size) {
            val len = min(maxBody, payload.size - offset)
            bodies.add(payload.copyOfRange(offset, offset + len))
            offset += len
        }
        val total = bodies.size
        return bodies.mapIndexed { index, body ->
            encode(msgId, index, total, body)
        }
    }

    fun maxBodyForMtu(mtu: Int): Int {
        val attMax = (mtu - 3).coerceAtLeast(MeshConstants.GATT_MIN_ATT_PAYLOAD)
        return (attMax - HEADER_BYTES).coerceIn(
            MeshConstants.GATT_MIN_CHUNK_BODY,
            MeshConstants.GATT_MAX_CHUNK_BODY
        )
    }

    private fun encode(msgId: Int, chunkIndex: Int, totalChunks: Int, body: ByteArray): ByteArray {
        val out = ByteBuffer.allocate(HEADER_BYTES + body.size).order(ByteOrder.BIG_ENDIAN)
        out.put(MAGIC)
        out.put(1.toByte())
        out.putInt(msgId)
        out.putShort(chunkIndex.toShort())
        out.putShort(totalChunks.toShort())
        out.putShort(body.size.toShort())
        out.put(body)
        return out.array()
    }

    data class ParsedChunk(
        val msgId: Int,
        val chunkIndex: Int,
        val totalChunks: Int,
        val body: ByteArray
    )

    fun parse(bytes: ByteArray): ParsedChunk? {
        if (!looksLikeChunk(bytes) || bytes.size < HEADER_BYTES) return null
        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        buf.get()
        buf.get()
        val version = buf.get().toInt() and 0xFF
        if (version != 1) return null
        val msgId = buf.int
        val idx = buf.short.toInt() and 0xFFFF
        val total = buf.short.toInt() and 0xFFFF
        val len = buf.short.toInt() and 0xFFFF
        if (total <= 0 || idx < 0 || idx >= total || len < 0) return null
        if (bytes.size != HEADER_BYTES + len) return null
        val body = ByteArray(len)
        buf.get(body)
        return ParsedChunk(msgId, idx, total, body)
    }
}
