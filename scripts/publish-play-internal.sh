#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION_NAME="${1:-${RELEASE_VERSION_NAME:-0.0.1}}"

if [[ ! "$VERSION_NAME" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Version must look like 0.0.1, got: $VERSION_NAME" >&2
  exit 1
fi

IFS='.' read -r MAJOR MINOR PATCH <<< "$VERSION_NAME"
VERSION_CODE=$((MAJOR * 10000 + MINOR * 100 + PATCH))

TEMP_DIR="$(mktemp -d)"
cleanup() {
  rm -rf "$TEMP_DIR"
}
trap cleanup EXIT

require_env() {
  local name="$1"
  if [[ -z "${!name:-}" ]]; then
    echo "Missing required environment variable: $name" >&2
    exit 1
  fi
}

require_env ANDROID_KEYSTORE_PASSWORD
require_env ANDROID_KEY_ALIAS
require_env ANDROID_KEY_PASSWORD

if [[ -n "${ANDROID_KEYSTORE_PATH:-}" ]]; then
  KEYSTORE_PATH="$ANDROID_KEYSTORE_PATH"
elif [[ -n "${ANDROID_KEYSTORE_BASE64:-}" ]]; then
  KEYSTORE_PATH="$TEMP_DIR/release.jks"
  printf '%s' "$ANDROID_KEYSTORE_BASE64" | base64 --decode > "$KEYSTORE_PATH"
else
  echo "Set ANDROID_KEYSTORE_PATH or ANDROID_KEYSTORE_BASE64." >&2
  exit 1
fi

if [[ -n "${PLAY_SERVICE_ACCOUNT_FILE:-}" ]]; then
  PLAY_CREDENTIALS_FILE="$PLAY_SERVICE_ACCOUNT_FILE"
elif [[ -n "${PLAY_SERVICE_ACCOUNT_JSON:-}" ]]; then
  PLAY_CREDENTIALS_FILE="$TEMP_DIR/play-account.json"
  printf '%s' "$PLAY_SERVICE_ACCOUNT_JSON" > "$PLAY_CREDENTIALS_FILE"
else
  echo "Set PLAY_SERVICE_ACCOUNT_FILE or PLAY_SERVICE_ACCOUNT_JSON." >&2
  exit 1
fi

"$ROOT_DIR/gradlew" \
  :app:publishReleaseBundle \
  -PreleaseVersionName="$VERSION_NAME" \
  -PreleaseVersionCode="$VERSION_CODE" \
  -PandroidKeystorePath="$KEYSTORE_PATH" \
  -PandroidKeystorePassword="$ANDROID_KEYSTORE_PASSWORD" \
  -PandroidKeyAlias="$ANDROID_KEY_ALIAS" \
  -PandroidKeyPassword="$ANDROID_KEY_PASSWORD" \
  -PplayServiceAccountFile="$PLAY_CREDENTIALS_FILE" \
  --no-daemon
