#!/bin/sh
# Gradle wrapper script - downloads and runs Gradle
GRADLE_VERSION="8.11.1"
GRADLE_HOME="${HOME}/.gradle/wrapper/dists/gradle-${GRADLE_VERSION}-bin"
GRADLE_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"

if [ ! -d "${GRADLE_HOME}" ]; then
    mkdir -p "${GRADLE_HOME}"
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    curl -L "${GRADLE_URL}" -o "${GRADLE_HOME}/gradle.zip"
    unzip -q "${GRADLE_HOME}/gradle.zip" -d "${GRADLE_HOME}"
    rm "${GRADLE_HOME}/gradle.zip"
fi

GRADLE_BIN=$(find "${GRADLE_HOME}" -name "gradle" -path "*/bin/gradle" | head -1)
exec "${GRADLE_BIN}" "$@"
