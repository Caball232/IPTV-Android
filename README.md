# 📺 StreamVault - Android & Amazon Firestick TV App

Universal native Android & Fire TV application client for **StreamVault IPTV**.

## 🌟 Key Features
- **Leanback Launcher Integration**: Shows up naturally on the Amazon Fire TV and Android TV home screens.
- **Fire TV Remote D-Pad Navigation**: Fully controllable using the standard Amazon Fire TV remote (UP/DOWN/LEFT/RIGHT/SELECT/BACK/MENU).
- **Dedicated Media Key Bridge**: Play, Pause, Rewind (-10s), and Fast-Forward (+10s) on Fire TV remotes directly control the stream.
- **Hardware-Accelerated Fullscreen**: Ultra-low latency HLS & MP4 playback with automatic aspect-ratio scaling.
- **On-Device Server URL Switcher**: Press the **MENU** button on your Fire TV remote anytime to point to a new local IP or cloud tunnel without reinstalling!
- **Universal Android Compatibility**: Runs on Fire OS 5, 6, 7, 8 (Android 5.1 through Android 14+).

---

## ⚡ 1-Click Cloud Build (GitHub Actions)

1. Run `build_apk_github.bat` or push this repository to GitHub.
2. Go to the **Actions** tab on your GitHub repository.
3. Once completed (~1 minute), download **StreamVault-FireTV.apk** from the Release or Artifact section!

---

## 📲 How to Install on Amazon Firestick via "Downloader"

The **Downloader** app by AFTVnews is the standard sideloading tool on all Fire TV devices.

### Step 1: Allow Apps from Unknown Sources
1. On Fire TV: Go to **Settings** > **My Fire TV** (or **Device & Software**).
2. Select **Developer Options**.
   - *(Note: If Developer Options is hidden, go to **About**, highlight your Fire TV name, and click the Center D-Pad button 7 times until it says "You are now a developer")*.
3. Turn **ON** "Install Unknown Apps" for **Downloader**.

### Step 2: Download the APK
1. Open the **Downloader** app on Firestick.
2. In the URL / Code box, enter your StreamVault APK URL:
   ```
   https://architectural-entry-shops-violations.trycloudflare.com/download/app.apk
   ```
   *(Or enter your AFTVnews shortcode created via `go.aftvnews.com`)*.
3. Click **Go**.
4. The download will start automatically, and Fire OS will prompt **Install**.
5. Click **Install**, then **Open** to launch StreamVault!
