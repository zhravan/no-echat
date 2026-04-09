package com.zhravan.noechat.ui.copy

import android.Manifest
import com.zhravan.noechat.data.local.PacketOrigin
import com.zhravan.noechat.domain.EmergencyStatus

/** Short user-facing phrases, avoid jargon (mesh, relay, hops, packets, etc.). */
object UserCopy {

    const val HOME_TAGLINE =
        "Nearby phones over Bluetooth. Works without cell or Wi‑Fi."

    const val READINESS_INTRO =
        "Allow Bluetooth and notifications so this phone can reach devices nearby."

    const val READINESS_CORE_BUTTON = "Allow Bluetooth & notifications"

    const val READINESS_LOCATION_INTRO =
        "Optional: for maps if you share location in an alert."

    const val READINESS_LOCATION_BUTTON = "Allow location (optional)"

    const val SOS_INTRO =
        "Nearby phones get your alert; they can pass it on."

    const val VOLUNTEER_INTRO =
        "Relay alerts between nearby phones when you want to. Turn off anytime."

    const val RESPONDER_INTRO = "From other phones in Bluetooth range."

    const val UPDATES_INTRO = "Sent here or relayed through this phone."

    fun emergencyStatus(status: EmergencyStatus): String = when (status) {
        EmergencyStatus.TRAPPED -> "Trapped / stuck"
        EmergencyStatus.INJURED -> "Injured"
        EmergencyStatus.MEDICAL -> "Medical emergency"
        EmergencyStatus.EVACUATION -> "Need evacuation"
        EmergencyStatus.NEEDS_SUPPLIES -> "Need supplies"
        EmergencyStatus.SAFE -> "I'm safe"
        EmergencyStatus.OTHER -> "Other"
    }

    fun packetOrigin(origin: String): String = when (origin) {
        PacketOrigin.LOCAL -> "Started on this phone"
        PacketOrigin.RELAY -> "Came via other phones nearby"
        else -> origin
    }

    fun permissionShortLabel(fullPermission: String): String = when (fullPermission) {
        Manifest.permission.POST_NOTIFICATIONS -> "Notifications"
        Manifest.permission.BLUETOOTH_SCAN -> "Bluetooth: find nearby phones"
        Manifest.permission.BLUETOOTH_CONNECT -> "Bluetooth: connect"
        Manifest.permission.BLUETOOTH_ADVERTISE -> "Bluetooth: be discoverable"
        Manifest.permission.ACCESS_FINE_LOCATION -> "Precise location"
        else -> fullPermission.substringAfterLast('.')
            .replace('_', ' ')
            .lowercase()
            .replaceFirstChar { it.uppercaseChar() }
    }

    fun yesNo(granted: Boolean): String = if (granted) "Allowed" else "Not allowed yet"

    /** Long IDs are hard to read; keep enough to tell alerts apart. */
    fun shortReference(id: String, head: Int = 12): String =
        if (id.length <= head) id else id.take(head) + "…"

    fun nearbyPhonesLine(count: Int): String = when {
        count == 0 -> "Phones nearby right now: none seen (move closer or check Bluetooth)"
        count == 1 -> "Phones nearby right now: 1"
        else -> "Phones nearby right now: $count"
    }

    /** One line for the home status strip. */
    fun homeStatusLine(peerCount: Int, relayOn: Boolean): String =
        "${nearbyPhonesLine(peerCount)} · " +
            if (relayOn) "Forwarding alerts for others" else "Not forwarding alerts"
}
