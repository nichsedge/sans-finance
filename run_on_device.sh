#!/bin/bash

PACKAGE="com.sans.finance"

./gradlew assembleDebug --daemon --build-cache || exit 1

adb shell monkey -p $PACKAGE -c android.intent.category.LAUNCHER 1