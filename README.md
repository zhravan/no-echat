# no-echat

An offline-first emergency mesh for Android. No internet. No cell signal. Just Bluetooth.

---

## What it does

Devices running no-echat form a local mesh over **Bluetooth LE**. Emergency packets (SOS alerts, safe broadcasts) hop from phone to phone until they reach someone who can act on them.

### Core flows

| Action | What happens |
|--------|-------------|
| **SOS** | Sends a signed emergency packet with optional note and GPS location into the mesh |
| **I'm safe** | Broadcasts a safe-status packet to cancel or reassure nearby devices |
| **Volunteer relay** | Device silently forwards other people's alerts it hears |
| **Responder view** | Shows active alerts heard from nearby devices |
| **My alerts** | History of packets you sent or relayed |

---

## How the mesh works

- Each device **advertises** and **scans** simultaneously over BLE GATT
- Packets are signed with a per-device key pair (ECDSA) to prevent spoofing
- Packets are chunked, reassembled, and deduplicated across hops
- Expired packets are purged automatically; hop count is tracked per packet
- No internet, no WiFi, no server, purely device-to-device BLE

---

## Requirements

- Android with Bluetooth LE support
- Permissions: `BLUETOOTH_SCAN`, `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT`, `ACCESS_FINE_LOCATION`
- Bluetooth must be **on**: the mesh does not function without it

---

## Inspiration

Inspired by [2018](https://en.wikipedia.org/wiki/2018_(film)), the Malayalam disaster film about the 2018 Kerala floods, where ordinary people became heroes through collective action and solidarity. The film's subtitle, *"Every one is a hero"*, captures what no-echat is built around: giving anyone the tools to send help, receive help, or relay it forward, with no infrastructure required.

---

## Tech stack

- Kotlin + Jetpack Compose
- BLE GATT (custom service/characteristic UUIDs)
- Room (local packet store)
- Coroutines + StateFlow
