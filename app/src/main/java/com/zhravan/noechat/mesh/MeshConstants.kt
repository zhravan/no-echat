package com.zhravan.noechat.mesh

object MeshConstants {
    const val MAX_HOPS = 7
    const val MAX_WIRE_BYTES = 12 * 1024
    const val GATT_MIN_ATT_PAYLOAD = 20
    const val GATT_MIN_CHUNK_BODY = 16
    const val GATT_MAX_CHUNK_BODY = 512
    const val PEER_STALE_MS = 30_000L
    const val PEER_PRUNE_INTERVAL_MS = 10_000L
}
