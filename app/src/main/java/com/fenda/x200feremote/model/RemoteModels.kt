package com.fenda.x200feremote.model

import androidx.compose.ui.graphics.Color

enum class ButtonCategory {
    POWER_INPUT,
    AUDIO_TONE,
    LIGHT_CONTROL,
    LIGHT_COLOR,
    LIGHT_EFFECT,
    PLAYBACK_FM,
    NUMPAD
}

/**
 * Represents a single remote button with a customizable IR code and carrier frequency.
 */
data class RemoteButton(
    val id: String,
    val label: String,
    val category: ButtonCategory,
    val irCode: String,
    val carrierHz: Int = 38000,
    val colorHex: Long? = null
) {
    val swatchColor: Color?
        get() = colorHex?.let { Color(it) }
}

/**
 * Default Fenda X200FE / F&D T-series style NEC IR profile.
 *
 * Note: F&D uses NEC 38 kHz across its lineup, though exact hex codes vary by chassis/batch.
 * Every button below can be edited in-app (long-press any button or enable Edit Mode).
 */
object DefaultRemoteProfile {
    val buttons: List<RemoteButton> = listOf(
        // Power & Input
        RemoteButton("power", "Power", ButtonCategory.POWER_INPUT, "0x807F01FE"),
        RemoteButton("mute", "Mute", ButtonCategory.POWER_INPUT, "0x807FC13E"),
        RemoteButton("source_mode", "Source", ButtonCategory.POWER_INPUT, "0x807F31CE"),
        RemoteButton("input_bt", "Bluetooth", ButtonCategory.POWER_INPUT, "0x807F21DE"),
        RemoteButton("input_aux", "AUX", ButtonCategory.POWER_INPUT, "0x807FA15E"),
        RemoteButton("input_usb", "USB", ButtonCategory.POWER_INPUT, "0x807F619E"),
        RemoteButton("input_opt", "Optical", ButtonCategory.POWER_INPUT, "0x807FE11E"),
        RemoteButton("input_fm", "FM Radio", ButtonCategory.POWER_INPUT, "0x807F19E6"),

        // Audio & Tone
        RemoteButton("vol_up", "Vol +", ButtonCategory.AUDIO_TONE, "0x807F817E"),
        RemoteButton("vol_down", "Vol -", ButtonCategory.AUDIO_TONE, "0x807F41BE"),
        RemoteButton("bass_up", "Bass +", ButtonCategory.AUDIO_TONE, "0x807F51AE"),
        RemoteButton("bass_down", "Bass -", ButtonCategory.AUDIO_TONE, "0x807FD12E"),
        RemoteButton("treble_up", "Treble +", ButtonCategory.AUDIO_TONE, "0x807F718E"),
        RemoteButton("treble_down", "Treble -", ButtonCategory.AUDIO_TONE, "0x807FF10E"),
        RemoteButton("eq_preset", "EQ", ButtonCategory.AUDIO_TONE, "0x807F09F6"),
        RemoteButton("reset_audio", "Reset", ButtonCategory.AUDIO_TONE, "0x807F8976"),

        // Light Control
        RemoteButton("light_power", "Light On/Off", ButtonCategory.LIGHT_CONTROL, "0x807F49B6"),
        RemoteButton("light_mode", "Light Mode", ButtonCategory.LIGHT_CONTROL, "0x807FC936"),
        RemoteButton("light_bright_up", "Bright +", ButtonCategory.LIGHT_CONTROL, "0x807F29D6"),
        RemoteButton("light_bright_down", "Bright -", ButtonCategory.LIGHT_CONTROL, "0x807FA956"),

        // Light Colors
        RemoteButton("light_color_red", "Red", ButtonCategory.LIGHT_COLOR, "0x807F6996", colorHex = 0xFFFF3B30),
        RemoteButton("light_color_orange", "Orange", ButtonCategory.LIGHT_COLOR, "0x807FE916", colorHex = 0xFFFF9500),
        RemoteButton("light_color_yellow", "Yellow", ButtonCategory.LIGHT_COLOR, "0x807F11EE", colorHex = 0xFFFFCC00),
        RemoteButton("light_color_green", "Green", ButtonCategory.LIGHT_COLOR, "0x807F916E", colorHex = 0xFF34C759),
        RemoteButton("light_color_cyan", "Cyan", ButtonCategory.LIGHT_COLOR, "0x807F59A6", colorHex = 0xFF32ADE6),
        RemoteButton("light_color_blue", "Blue", ButtonCategory.LIGHT_COLOR, "0x807FD926", colorHex = 0xFF007AFF),
        RemoteButton("light_color_magenta", "Magenta", ButtonCategory.LIGHT_COLOR, "0x807F39C6", colorHex = 0xFFAF52DE),
        RemoteButton("light_color_white", "White", ButtonCategory.LIGHT_COLOR, "0x807FB946", colorHex = 0xFFF2F2F7),

        // Light Effects
        RemoteButton("light_fx_breath", "Breathing", ButtonCategory.LIGHT_EFFECT, "0x807F7986"),
        RemoteButton("light_fx_strobe", "Strobe", ButtonCategory.LIGHT_EFFECT, "0x807FF906"),
        RemoteButton("light_fx_music", "Music Sync", ButtonCategory.LIGHT_EFFECT, "0x807F05FA"),
        RemoteButton("light_fx_rainbow", "Rainbow", ButtonCategory.LIGHT_EFFECT, "0x807F857A"),

        // Playback & FM Tuner
        RemoteButton("play_pause", "Play/Pause", ButtonCategory.PLAYBACK_FM, "0x807F45BA"),
        RemoteButton("prev_track", "Prev", ButtonCategory.PLAYBACK_FM, "0x807FC53A"),
        RemoteButton("next_track", "Next", ButtonCategory.PLAYBACK_FM, "0x807F25DA"),
        RemoteButton("repeat", "Repeat", ButtonCategory.PLAYBACK_FM, "0x807FA55A"),
        RemoteButton("fm_scan", "FM Scan", ButtonCategory.PLAYBACK_FM, "0x807F659A"),
        RemoteButton("ch_prev", "CH -", ButtonCategory.PLAYBACK_FM, "0x807FE51A"),
        RemoteButton("ch_next", "CH +", ButtonCategory.PLAYBACK_FM, "0x807F15EA"),

        // Numpad
        RemoteButton("num_1", "1", ButtonCategory.NUMPAD, "0x807F956A"),
        RemoteButton("num_2", "2", ButtonCategory.NUMPAD, "0x807F55AA"),
        RemoteButton("num_3", "3", ButtonCategory.NUMPAD, "0x807FD52A"),
        RemoteButton("num_4", "4", ButtonCategory.NUMPAD, "0x807F35CA"),
        RemoteButton("num_5", "5", ButtonCategory.NUMPAD, "0x807FB54A"),
        RemoteButton("num_6", "6", ButtonCategory.NUMPAD, "0x807F758A"),
        RemoteButton("num_7", "7", ButtonCategory.NUMPAD, "0x807FF50A"),
        RemoteButton("num_8", "8", ButtonCategory.NUMPAD, "0x807F0DF2"),
        RemoteButton("num_9", "9", ButtonCategory.NUMPAD, "0x807F8D72"),
        RemoteButton("num_0", "0", ButtonCategory.NUMPAD, "0x807F4DB2")
    )
}
