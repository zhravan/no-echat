package com.zhravan.noechat

import android.app.Application
import com.zhravan.noechat.data.local.NoEchatDatabase
import com.zhravan.noechat.data.repository.DefaultPacketRepository
import com.zhravan.noechat.domain.PacketRepository
import com.zhravan.noechat.identity.ActiveAlertStore
import com.zhravan.noechat.identity.DeviceIdentityStore
import com.zhravan.noechat.mesh.GattMeshTransport
import com.zhravan.noechat.mesh.MeshCoordinator
import com.zhravan.noechat.mesh.crypto.KeystorePacketSigner
import com.zhravan.noechat.mesh.crypto.PacketSigner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NoEchatApplication : Application() {

    private val database by lazy { NoEchatDatabase.build(this) }
    private val identityStore by lazy { DeviceIdentityStore(this) }
    val activeAlertStore by lazy { ActiveAlertStore(this) }
    private val packetSigner: PacketSigner by lazy { KeystorePacketSigner(this) }
    private val meshTransport by lazy { GattMeshTransport(this) }

    val packetRepository: PacketRepository by lazy {
        DefaultPacketRepository(
            dao = database.emergencyPacketDao(),
            identity = identityStore,
            signer = packetSigner
        )
    }

    val meshCoordinator: MeshCoordinator by lazy {
        MeshCoordinator(
            repository = packetRepository,
            transport = meshTransport
        )
    }

    private val relayRunningInternal = MutableStateFlow(false)
    val relayRunning: StateFlow<Boolean> = relayRunningInternal.asStateFlow()

    internal fun setRelayRunning(value: Boolean) {
        relayRunningInternal.value = value
    }
}
