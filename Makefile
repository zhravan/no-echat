# NoEchat: Android project. Requires JDK 17+.
#
# Gradle needs the Android SDK. This Makefile sets ANDROID_HOME when it is
# empty and a common install path exists. Otherwise set it yourself, e.g.:
#   export ANDROID_HOME=$HOME/Library/Android/sdk   # macOS
#   export ANDROID_HOME=$HOME/Android/Sdk           # Linux
#
# Optional: pin SDK for Android Studio — same path as sdk.dir:
#   make local-properties

GRADLEW := ./gradlew
GRADLE_FLAGS ?= --no-daemon

# If ANDROID_HOME is unset or empty, use default SDK path when it looks installed.
ifeq ($(strip $(ANDROID_HOME)),)
  ifeq ($(shell uname -s),Darwin)
    _DEFAULT_SDK := $(HOME)/Library/Android/sdk
  else
    _DEFAULT_SDK := $(HOME)/Android/Sdk
  endif
  ifneq ($(wildcard $(_DEFAULT_SDK)/platform-tools),)
    export ANDROID_HOME := $(_DEFAULT_SDK)
  endif
endif

export ANDROID_SDK_ROOT ?= $(ANDROID_HOME)

.PHONY: default help build test check clean lint assemble-debug local-properties run-debug

default: help

help:
	@echo "NoEchat Makefile"
	@echo ""
	@echo "  make build             Assemble debug APK (:app:assembleDebug)"
	@echo "  make assemble-debug    Same as build"
	@echo "  make test              Run JVM unit tests (testDebugUnitTest)"
	@echo "  make check             Run tests then assemble debug"
	@echo "  make lint              Run Android lint (lintDebug)"
	@echo "  make clean             Gradle clean"
	@echo "  make local-properties  Write local.properties from ANDROID_HOME"
	@echo "  make run-debug           Install debug APK on USB device and launch app"
	@echo ""
	@if [ -n "$(ANDROID_HOME)" ]; then \
		echo "Using ANDROID_HOME=$(ANDROID_HOME)"; \
	else \
		echo "ANDROID_HOME not set (and default SDK path not found)."; \
	fi
	@echo "Override flags: make GRADLE_FLAGS='--no-daemon --warning-mode=all' build"

build: assemble-debug

assemble-debug:
	@test -n "$(ANDROID_HOME)" || (echo "SDK not found. Install Android SDK, or:"; echo "  export ANDROID_HOME=</path/to/sdk>"; echo "  make local-properties   # after ANDROID_HOME is set"; exit 1)
	$(GRADLEW) $(GRADLE_FLAGS) :app:assembleDebug

test:
	@test -n "$(ANDROID_HOME)" || (echo "SDK not found; set ANDROID_HOME (see make help)."; exit 1)
	$(GRADLEW) $(GRADLE_FLAGS) testDebugUnitTest

check: test assemble-debug

clean:
	$(GRADLEW) $(GRADLE_FLAGS) clean

lint:
	@test -n "$(ANDROID_HOME)" || (echo "SDK not found; set ANDROID_HOME (see make help)."; exit 1)
	$(GRADLEW) $(GRADLE_FLAGS) :app:lintDebug

local-properties:
	@test -n "$(ANDROID_HOME)" || (echo "Set ANDROID_HOME to your SDK path first."; exit 1)
	@echo "sdk.dir=$(ANDROID_HOME)" > local.properties
	@echo "Wrote local.properties -> sdk.dir=$(ANDROID_HOME)"

run-debug:
	@test -n "$(ANDROID_HOME)" || (echo "SDK not found; set ANDROID_HOME (see make help)."; exit 1)
	@GRADLE_FLAGS="$(GRADLE_FLAGS)" "$(CURDIR)/scripts/run-debug-device.sh"
