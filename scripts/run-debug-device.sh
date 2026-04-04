#!/usr/bin/env bash
# Build, install debug APK on a USB (or adb) device, and launch the app.
# Requires: USB debugging enabled, device authorized. One device unless ANDROID_SERIAL is set.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ -z "${ANDROID_HOME:-}" ]]; then
  case "$(uname -s)" in
    Darwin) _def="$HOME/Library/Android/sdk" ;;
    *) _def="$HOME/Android/Sdk" ;;
  esac
  if [[ -d "$_def/platform-tools" ]]; then
    export ANDROID_HOME="$_def"
  fi
fi

if [[ -z "${ANDROID_HOME:-}" ]]; then
  echo "SDK not found. Set ANDROID_HOME or install the Android SDK." >&2
  exit 1
fi

ADB="$ANDROID_HOME/platform-tools/adb"
if [[ ! -x "$ADB" ]]; then
  echo "adb not found at $ADB" >&2
  exit 1
fi

print_usb_help() {
  echo "Troubleshooting (phone must appear under 'adb devices' as 'device'):" >&2
  echo "  - Use a data-capable USB cable (many cables are charge-only)." >&2
  echo "  - On the phone: Settings → Developer options → USB debugging ON." >&2
  echo "  - When plugging in, choose USB mode 'File transfer' / 'MTP' (not charge only)." >&2
  echo "  - If you see 'unauthorized': unlock the phone and confirm the RSA fingerprint dialog." >&2
  echo "  - Try: \"$ADB\" kill-server && \"$ADB\" start-server && \"$ADB\" devices" >&2
  echo "  - Wireless: Developer options → Wireless debugging → pair, then adb connect <ip>:<port>" >&2
}

pick_serial() {
  # Wake adb daemon (helps right after cable connect or first run).
  "$ADB" start-server >/dev/null 2>&1 || true

  if [[ -n "${ANDROID_SERIAL:-}" ]]; then
    if ! "$ADB" -s "$ANDROID_SERIAL" get-state 2>/dev/null | grep -qx device; then
      echo "ANDROID_SERIAL=$ANDROID_SERIAL is not connected or not authorized." >&2
      "$ADB" devices >&2
      print_usb_help
      exit 1
    fi
    export ANDROID_SERIAL
    return
  fi

  local -a ready=()
  local saw_unauthorized=0
  local saw_offline=0
  local saw_any=0

  while read -r ser state _rest; do
    [[ -z "$ser" || "$ser" == "List" ]] && continue
    saw_any=1
    case "$state" in
      device) ready+=("$ser") ;;
      unauthorized) saw_unauthorized=1 ;;
      offline) saw_offline=1 ;;
    esac
  done < <("$ADB" devices 2>/dev/null | awk 'NR>1 && NF>=2 {print $1, $2}')

  if [[ ${#ready[@]} -gt 1 ]]; then
    echo "Multiple devices attached. Pick one and run again, e.g.:" >&2
    for d in "${ready[@]}"; do echo "  ANDROID_SERIAL=$d $0" >&2; done
    exit 1
  fi
  if [[ ${#ready[@]} -eq 1 ]]; then
    export ANDROID_SERIAL="${ready[0]}"
    return
  fi

  echo "No usable device for installDebug." >&2
  "$ADB" devices >&2
  echo "" >&2
  if [[ "$saw_unauthorized" -eq 1 ]]; then
    echo "A device is connected but not authorized. Unlock the phone and allow USB debugging (RSA dialog)." >&2
    echo "" >&2
  elif [[ "$saw_offline" -eq 1 ]]; then
    echo "A device reports 'offline'. Unplug/replug the cable or restart adb (see below)." >&2
    echo "" >&2
  elif [[ "$saw_any" -eq 0 ]]; then
    echo "Nothing listed under adb. The Mac is not seeing an Android device over USB." >&2
    echo "" >&2
  fi
  print_usb_help
  exit 1
}

pick_serial

GRADLE_FLAGS="${GRADLE_FLAGS:---no-daemon}"
./gradlew $GRADLE_FLAGS :app:installDebug

"$ADB" shell am start -n com.zhravan.noechat/.MainActivity

echo "Installed and started debug build on $ANDROID_SERIAL."
