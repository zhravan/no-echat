package com.zhravan.noechat.mesh

import java.util.concurrent.ConcurrentHashMap

class GattChunkReassembly(
    private val staleMs: Long = 60_000L
) {

    private data class Session(
        val total: Int,
        val parts: Array<ByteArray?>,
        var received: Int,
        val createdAtMs: Long
    )

    private val sessions = ConcurrentHashMap<Key, Session>()

    private data class Key(val address: String, val msgId: Int)

    fun clear() {
        sessions.clear()
    }

    fun feed(deviceAddress: String, bytes: ByteArray, nowMs: Long = System.currentTimeMillis()): ByteArray? {
        val parsed = GattChunkFramer.parse(bytes) ?: return null
        prune(nowMs)
        val key = Key(deviceAddress, parsed.msgId)
        val session = sessions.compute(key) { _, existing ->
            when {
                existing == null -> Session(
                    total = parsed.totalChunks,
                    parts = arrayOfNulls(parsed.totalChunks),
                    received = 0,
                    createdAtMs = nowMs
                )
                existing.total != parsed.totalChunks -> Session(
                    total = parsed.totalChunks,
                    parts = arrayOfNulls(parsed.totalChunks),
                    received = 0,
                    createdAtMs = nowMs
                )
                else -> existing
            }
        } ?: return null
        if (parsed.chunkIndex >= session.total) return null
        if (session.parts[parsed.chunkIndex] == null) {
            session.parts[parsed.chunkIndex] = parsed.body
            session.received++
        }
        if (session.received < session.total) return null
        sessions.remove(key)
        var size = 0
        for (p in session.parts) {
            if (p == null) return null
            size += p.size
        }
        val out = ByteArray(size)
        var pos = 0
        for (p in session.parts) {
            p!!.copyInto(out, pos)
            pos += p.size
        }
        return out
    }

    private fun prune(nowMs: Long) {
        val it = sessions.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (nowMs - entry.value.createdAtMs > staleMs) {
                it.remove()
            }
        }
    }
}
