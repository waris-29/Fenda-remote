# Fenda X200FE Smart IR Remote (Android / Kotlin)

A native Android (Kotlin + Jetpack Compose) IR remote control app for the **Fenda X200FE** home theater system, covering LED lights, light colors, effects, audio tone, input sources, playback, FM tuner, and numpad controls.

## Features
- **Smart Lights & Colors**: Dedicated panel for Light On/Off, Light Mode, Brightness +/-, 8 color swatches (Red, Orange, Yellow, Green, Cyan, Blue, Magenta, White), and 4 effect modes (Breathing, Strobe, Music Sync, Rainbow).
- **Full Remote Functions**: Power, Mute, Source/Input (Bluetooth, AUX, USB, Optical, FM Radio), Volume +/-, Bass +/-, Treble +/-, EQ, Reset, Playback (Play/Pause, Prev, Next, Repeat), FM Tuner (Auto Scan, CH +/-), and 0–9 Numpad.
- **Built-in IR Code Editor**: Long-press any button (or toggle **Edit Codes**) to customize its label, carrier frequency, and IR code. Supports:
  - **32-bit NEC Hex**: e.g. `0x807F01FE`
  - **Address:Command Hex**: e.g. `80:01` (auto-computes inverse bytes)
  - **Pronto Hex**: e.g. `0000 006D ...`
  - **Raw Microsecond Pulses**: e.g. `9000, 4500, 562, 562, ...`
- **Test Fire & JSON Backup**: Test-fire any edited code immediately and export/import your full button profile as JSON.
- **Preview Mode**: Launches cleanly even on phones without an IR blaster so you can inspect, edit, and export profiles.

## Requirements
- Android 8.0 (API 26) or higher.
- A smartphone with a built-in **IR Blaster** (`ConsumerIrManager`) to transmit signals to the speaker.

## Building & Running
1. Open the project folder (`fenda-x200fe-remote`) in **Android Studio** (Ladybug or newer recommended).
2. Let Gradle sync the dependencies (`compileSdk = 35`, Kotlin `2.0.21`, Compose BOM `2024.11.00`).
3. Connect your IR-equipped Android phone via USB debugging and click **Run**.

## Customizing Codes for Your Exact Unit
F&D / Fenda speakers use the 38 kHz NEC IR protocol, though exact hex codes can vary across chassis and production batches. If any default button doesn't respond on your X200FE:
1. Capture the exact NEC hex from your physical remote (e.g., using an Arduino/ESP32 + TSOP1838 IR receiver with `IRremote` `ReceiveDump`).
2. Tap **Edit Codes** (or long-press the button) in the app, paste the NEC hex (e.g. `0x807F01FE`), tap **Test Fire**, and **Save**.
3. Use the **JSON** icon in the top bar to back up or share your tuned profile.
