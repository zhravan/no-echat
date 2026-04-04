package com.zhravan.noechat.mesh

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface MeshTransport {
    val peerCount: StateFlow<Int>

    val relayStats: StateFlow<RelayStats>

    fun start(parentScope: CoroutineScope, onPayload: suspend (ByteArray) -> Unit)

    fun stop()

    suspend fun relayBroadcast(payload: ByteArray): Boolean
}
