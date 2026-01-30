# Greyscale Scheduler

This app toggles system-wide grayscale by writing secure settings. That requires the
privileged permission `WRITE_SECURE_SETTINGS`. It cannot be granted by the app at runtime,
so you must grant it via ADB.

## Grant WRITE_SECURE_SETTINGS via ADB


### 1) Enable Developer Options and USB debugging
1. Open Settings -> About phone.
2. Tap "Build number" 7 times to enable Developer Options.
3. Go back to Settings -> Developer options.
4. Enable "USB debugging".
   - On some OEMs (e.g., Samsung/Xiaomi), you may also need:
     - "USB debugging (Security settings)" or
     - "Install via USB"

### 2) Connect the device and authorize ADB
1. Connect your device via USB.
2. On your computer, verify ADB sees the device:
   - `adb devices`
3. If prompted on the device, allow USB debugging for this computer.
   - You should see your device listed as `device` (not `unauthorized`).

### 3) Grant the permission
Grant the permission:
- `adb shell pm grant de.nielstron.scheduler android.permission.WRITE_SECURE_SETTINGS`

### 4) Verify
1. Open the app.
2. The "Secure settings permission granted" message should appear.
3. Tap "Start grayscale now" to test.

## Revoke (if needed)
You can revoke it with:
- `adb shell pm revoke de.nielstron.scheduler android.permission.WRITE_SECURE_SETTINGS`

## Troubleshooting
- If `adb devices` shows `unauthorized`, unplug/replug and accept the prompt on the device.
- If `pm grant` fails:
  - Confirm USB debugging is enabled.
  - Check for OEM-specific "USB debugging (Security settings)" toggles.
  - Make sure the app is installed on the device before running `pm grant`.
  - Common Android Studio install path: `~/Library/Android/sdk/platform-tools/adb`
