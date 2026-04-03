package com.zhravan.noechat

import android.app.Application
import com.zhravan.noechat.data.local.NoEchatDatabase
import com.zhravan.noechat.data.repository.DefaultPacketRepository
import com.zhravan.noechat.domain.PacketRepository
import com.zhravan.noechat.identity.DeviceIdentityStore

class NoEchatApplication : Application() {

    private val database by lazy { NoEchatDatabase.build(this) }
    private val identityStore by lazy { DeviceIdentityStore(this) }

    val packetRepository: PacketRepository by lazy {
        DefaultPacketRepository(
            dao = database.emergencyPacketDao(),
            identity = identityStore
        )
    }
}
