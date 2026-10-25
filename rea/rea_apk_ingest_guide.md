# Supplying Lark Player APK for REA Inspection

To perform deep static analysis and decompilation of Lark Player using REA:

### Option 1: Direct File Placement
If you already have a Lark Player `.apk` file:
1. Copy or move your `.apk` file to:
   ```
   C:\Users\pc\.gemini\antigravity\scratch\lark-music-android\rea\lark_target.apk
   ```
2. Notify the assistant, and REA will immediately run `inspect_android_package`, `search_android_classes`, and decompilation passes.

---

### Option 2: Pulling from Android Phone via ADB
If you have Lark Player installed on your Android device:
1. Enable **Developer Options** and **USB Debugging** on your phone.
2. Connect your phone via USB to your computer.
3. Find the Lark Player package name:
   ```powershell
   adb shell pm list packages | Select-String -Pattern "lark|player|music"
   ```
4. Get the path to the installed APK:
   ```powershell
   adb shell pm path com.gemini.music.player
   ```
5. Pull the APK:
   ```powershell
   adb pull <path-from-previous-step> "C:\Users\pc\.gemini\antigravity\scratch\lark-music-android\rea\lark_target.apk"
   ```

---

### Option 3: Android Extraction Apps
You can use free utilities like **APK Extractor** or **ML Manager** from Google Play on your phone:
1. Open the app on your phone.
2. Select **Lark Player** and tap **Extract**.
3. Send the generated APK to your computer via Bluetooth, Google Drive, or USB cable, and place it in the `rea/` folder above.
