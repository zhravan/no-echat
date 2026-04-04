package com.zhravan.noechat.mesh

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.random.Random

@SuppressLint("MissingPermission")
class GattMeshTransport(
    private val context: Context,
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
) : MeshTransport {

    private val _peerCount = MutableStateFlow(0)
    override val peerCount: StateFlow<Int> = _peerCount.asStateFlow()

    private val peerLastSeen = ConcurrentHashMap<String, Long>()
    private val chunkReassembly = GattChunkReassembly()
    private var pruneJob: Job? = null

    private var parentScope: CoroutineScope? = null
    private var payloadHandler: (suspend (ByteArray) -> Unit)? = null

    private var gattServer: BluetoothGattServer? = null
    private var scanner: BluetoothLeScanner? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var scanCallback: ScanCallback? = null
    private var advertiseCallback: AdvertiseCallback? = null

    override fun start(parentScope: CoroutineScope, onPayload: suspend (ByteArray) -> Unit) {
        stop()
        if (!hasConnectPermission() || !hasScanPermission()) return
        val adapter = bluetoothManager.adapter ?: return
        if (!adapter.isEnabled) return
        this.parentScope = parentScope
        this.payloadHandler = onPayload
        scanner = adapter.bluetoothLeScanner
        advertiser = adapter.bluetoothLeAdvertiser
        parentScope.launch(Dispatchers.Main) {
            openGattServer()
        }
        startAdvertising(adapter)
        startScan()
        pruneJob = parentScope.launch {
            while (isActive) {
                delay(MeshConstants.PEER_PRUNE_INTERVAL_MS)
                pruneStalePeers()
            }
        }
    }

    override fun stop() {
        pruneJob?.cancel()
        pruneJob = null
        stopScan()
        stopAdvertising()
        gattServer?.services?.forEach { svc ->
            gattServer?.removeService(svc)
        }
        gattServer?.close()
        gattServer = null
        parentScope = null
        payloadHandler = null
        chunkReassembly.clear()
        peerLastSeen.clear()
        _peerCount.value = 0
    }

    override suspend fun relayBroadcast(payload: ByteArray): Boolean = withContext(Dispatchers.IO) {
        if (!hasConnectPermission()) return@withContext false
        val adapter = bluetoothManager.adapter ?: return@withContext false
        if (!adapter.isEnabled) return@withContext false
        if (payload.size > MeshConstants.MAX_WIRE_BYTES) return@withContext false
        val targets = peerLastSeen.keys.toList()
        if (targets.isEmpty()) return@withContext false
        var anySuccess = false
        for (address in targets) {
            val ok = writePayloadToPeer(adapter, address, payload)
            if (ok) anySuccess = true
        }
        anySuccess
    }

    private suspend fun writePayloadToPeer(
        adapter: BluetoothAdapter,
        address: String,
        payload: ByteArray
    ): Boolean {
        val device = runCatching { adapter.getRemoteDevice(address) }.getOrNull() ?: return false
        return suspendCancellableCoroutine { cont ->
            val finished = AtomicBoolean(false)
            fun end(value: Boolean) {
                if (finished.compareAndSet(false, true) && cont.isActive) cont.resume(value)
            }
            val msgId = Random.nextInt()
            var negotiatedMtu = 23
            var framedChunks: List<ByteArray> = emptyList()
            var chunkIdx = 0
            var meshCharacteristic: BluetoothGattCharacteristic? = null

            fun writeNextChunk(gatt: BluetoothGatt) {
                val char = meshCharacteristic ?: run {
                    gatt.disconnect()
                    return
                }
                if (chunkIdx >= framedChunks.size) return
                val frame = framedChunks[chunkIdx]
                char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                val queued = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val result = gatt.writeCharacteristic(
                        char,
                        frame,
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    )
                    result == BluetoothGatt.GATT_SUCCESS
                } else {
                    @Suppress("DEPRECATION")
                    char.value = frame
                    @Suppress("DEPRECATION")
                    gatt.writeCharacteristic(char)
                }
                if (!queued) {
                    gatt.disconnect()
                }
            }

            val gatt = device.connectGatt(
                context.applicationContext,
                false,
                object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            gatt.requestMtu(512)
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            gatt.close()
                            end(false)
                        }
                    }

                    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            negotiatedMtu = mtu
                        }
                        gatt.discoverServices()
                    }

                    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                        if (status != BluetoothGatt.GATT_SUCCESS) {
                            gatt.disconnect()
                            return
                        }
                        val service = gatt.getService(NOECHAT_SERVICE) ?: run {
                            gatt.disconnect()
                            return
                        }
                        val characteristic = service.getCharacteristic(NOECHAT_WRITE) ?: run {
                            gatt.disconnect()
                            return
                        }
                        meshCharacteristic = characteristic
                        val maxBody = GattChunkFramer.maxBodyForMtu(negotiatedMtu)
                        framedChunks = GattChunkFramer.split(payload, maxBody, msgId)
                        if (framedChunks.isEmpty()) {
                            end(true)
                            gatt.disconnect()
                            return
                        }
                        chunkIdx = 0
                        writeNextChunk(gatt)
                    }

                    override fun onCharacteristicWrite(
                        gatt: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic,
                        status: Int
                    ) {
                        if (status != BluetoothGatt.GATT_SUCCESS) {
                            end(false)
                            gatt.disconnect()
                            return
                        }
                        chunkIdx++
                        if (chunkIdx >= framedChunks.size) {
                            end(true)
                            gatt.disconnect()
                        } else {
                            writeNextChunk(gatt)
                        }
                    }
                },
                BluetoothDevice.TRANSPORT_LE
            )
            cont.invokeOnCancellation { runCatching { gatt.close() } }
        }
    }

    private fun openGattServer() {
        val handler = payloadHandler ?: return
        val scope = parentScope ?: return
        val callback = object : BluetoothGattServerCallback() {
            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?
            ) {
                if (characteristic.uuid == NOECHAT_WRITE && value != null && value.isNotEmpty()) {
                    val address = device.address ?: return
                    if (GattChunkFramer.looksLikeChunk(value)) {
                        val complete = chunkReassembly.feed(address, value)
                        if (complete != null) {
                            scope.launch(Dispatchers.IO) { handler(complete) }
                        }
                    } else {
                        scope.launch(Dispatchers.IO) { handler(value.copyOf()) }
                    }
                }
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                }
            }
        }
        gattServer = bluetoothManager.openGattServer(context.applicationContext, callback)
        val service = BluetoothGattService(
            NOECHAT_SERVICE,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        val characteristic = BluetoothGattCharacteristic(
            NOECHAT_WRITE,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ
        )
        service.addCharacteristic(characteristic)
        gattServer?.addService(service)
    }

    private fun startScan() {
        if (!hasScanPermission()) return
        val sc = scanner ?: return
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(NOECHAT_SERVICE))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val addr = result.device?.address ?: return
                peerLastSeen[addr] = System.currentTimeMillis()
                _peerCount.value = peerLastSeen.size
            }
        }
        runCatching { sc.startScan(listOf(filter), settings, scanCallback) }
    }

    private fun stopScan() {
        val sc = scanner
        val cb = scanCallback
        if (sc != null && cb != null) {
            runCatching { sc.stopScan(cb) }
        }
        scanCallback = null
    }

    private fun pruneStalePeers() {
        val now = System.currentTimeMillis()
        val it = peerLastSeen.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (now - entry.value > MeshConstants.PEER_STALE_MS) {
                it.remove()
            }
        }
        _peerCount.value = peerLastSeen.size
    }

    private fun startAdvertising(adapter: BluetoothAdapter) {
        if (!hasAdvertisePermission()) return
        val adv = advertiser ?: return
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .build()
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(NOECHAT_SERVICE))
            .build()
        advertiseCallback = object : AdvertiseCallback() {}
        runCatching { adv.startAdvertising(settings, data, advertiseCallback) }
    }

    private fun stopAdvertising() {
        val adv = advertiser
        val cb = advertiseCallback
        if (adv != null && cb != null) {
            runCatching { adv.stopAdvertising(cb) }
        }
        advertiseCallback = null
    }

    private fun hasConnectPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

    private fun hasScanPermission(): Boolean =
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
            }
            else -> true
        }

    private fun hasAdvertisePermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED

    private companion object {
        val NOECHAT_SERVICE: UUID = UUID.fromString("6E400001-b5a3-f393-e0a9-e50e24dcca9e")
        val NOECHAT_WRITE: UUID = UUID.fromString("6E400002-b5a3-f393-e0a9-e50e24dcca9e")
    }
}
