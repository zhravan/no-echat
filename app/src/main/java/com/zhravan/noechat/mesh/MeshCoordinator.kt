package com.zhravan.noechat.mesh

import com.zhravan.noechat.domain.PacketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class MeshCoordinator(
    private val repository: PacketRepository,
    private val transport: MeshTransport
) {
    private var job: Job? = null
    private var scope: CoroutineScope? = null

    val peerCount: StateFlow<Int> = transport.peerCount

    fun start() {
        if (job?.isActive == true) return
        val supervisor = SupervisorJob()
        job = supervisor
        val s = CoroutineScope(supervisor + Dispatchers.Default)
        scope = s
        transport.start(s) { bytes ->
            repository.ingestFromWire(bytes)
        }
        s.launch {
            repository.observePendingRelay()
                .debounce(500)
                .collect { pending ->
                    for (packet in pending) {
                        val bytes = repository.encodeForRelay(packet.publicId) ?: continue
                        if (bytes.size > MeshConstants.MAX_WIRE_BYTES) {
                            repository.markRelayedOut(packet.publicId)
                            continue
                        }
                        val ok = transport.relayBroadcast(bytes)
                        if (ok) repository.markRelayedOut(packet.publicId)
                    }
                }
        }
    }

    fun stop() {
        transport.stop()
        job?.cancel()
        job = null
        scope = null
    }
}
