#!/bin/bash
cd "$(dirname "$0")/.." || exit

PACKAGE_NAME="com.sans.finance"
DB_NAME="sans_finance_db"
TEMP_DB="sans_finance_db"

echo "🚀 Starting Portfolio Backfill Automation..."

# 1. Run on device (Build & Launch)
echo "📱 Ensuring app is installed and running..."
./scripts/run_on_device.sh || exit 1

# 2. Pull Database from device
echo "📥 Pulling database from device..."
adb shell "run-as $PACKAGE_NAME cat databases/$DB_NAME" > $TEMP_DB
if [ $? -ne 0 ]; then
    echo "❌ Error: Failed to pull database. Is the device connected and app installed?"
    exit 1
fi

# 3. Run Python script to backfill data
echo "🐍 Running backfill script..."
python3 ./scripts/backfill_portfolio.py
if [ $? -ne 0 ]; then
    echo "❌ Error: Python backfill script failed."
    rm $TEMP_DB
    exit 1
fi

# 4. Push Database back to device
echo "📤 Pushing updated database to device..."
adb push $TEMP_DB /data/local/tmp/$TEMP_DB
adb shell "chmod 666 /data/local/tmp/$TEMP_DB"
adb shell "run-as $PACKAGE_NAME sh -c 'cat /data/local/tmp/$TEMP_DB > databases/$DB_NAME'"

# 5. Clean up intermediate files
echo "🧹 Cleaning up..."
adb shell "run-as $PACKAGE_NAME rm databases/$DB_NAME-wal databases/$DB_NAME-shm 2>/dev/null"
adb shell "rm /data/local/tmp/$TEMP_DB"
rm $TEMP_DB

# 6. Restart App
echo "🚀 Restarting application..."
adb shell am force-stop $PACKAGE_NAME
adb shell am start -n $PACKAGE_NAME/.MainActivity

echo "✅ Portfolio backfill completed successfully!"
