#!/bin/bash
cd "$(dirname "$0")/.." || exit

PACKAGE="com.sans.finance"

./gradlew installDebug --daemon --build-cache || exit 1

adb shell am start -n "$PACKAGE/.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER