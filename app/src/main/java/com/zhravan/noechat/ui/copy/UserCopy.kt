package com.zhravan.noechat.ui.copy

import android.Manifest
import com.zhravan.noechat.data.local.PacketOrigin
import com.zhravan.noechat.domain.EmergencyStatus

/** Short user-facing phrases — avoid jargon (mesh, relay, hops, packets, etc.). */
object UserCopy {

    const val HOME_TAGLINE =
        "Send help requests to nearby phones over Bluetooth. No cell or Wi-Fi needed."

    const val HOME_SOS_HINT = "Use when you need help from people around you."

    const val READINESS_INTRO =
        "NoEchat needs a few phone settings so it can find and talk to nearby devices."

    const val READINESS_CORE_BUTTON = "Allow Bluetooth & notifications"

    const val READINESS_LOCATION_INTRO =
        "Optional: location helps show where you are on a map if you choose to share it in an alert."

    const val READINESS_LOCATION_BUTTON = "Allow location (optional)"

    const val SOS_INTRO =
        "This sends your alert to nearby phones. They can pass it along further."

    const val VOLUNTEER_INTRO =
        "When this is on, your phone can quietly pass alerts between other nearby phones. " +
            "You stay in control and can turn it off anytime."

    const val RESPONDER_INTRO = "Alerts heard from people nearby (via Bluetooth)."

    const val UPDATES_INTRO = "Alerts you sent or that passed through this phone."

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
        Manifest.permission.BLUETOOTH_SCAN -> "Bluetooth — find nearby phones"
        Manifest.permission.BLUETOOTH_CONNECT -> "Bluetooth — connect"
        Manifest.permission.BLUETOOTH_ADVERTISE -> "Bluetooth — be discoverable"
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

    fun relayLine(relayOn: Boolean): String =
        if (relayOn) "Helping pass alerts for others: on"
        else "Helping pass alerts for others: off"

    fun nearbyPhonesLine(count: Int): String = when {
        count == 0 -> "Phones nearby right now: none seen (move closer or check Bluetooth)"
        count == 1 -> "Phones nearby right now: 1"
        else -> "Phones nearby right now: $count"
    }
}
