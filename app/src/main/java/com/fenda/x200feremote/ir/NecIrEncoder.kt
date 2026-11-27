package com.fenda.x200feremote.ir

/**
 * Represents a ready-to-transmit infrared signal.
 *
 * @property carrierFrequencyHz Modulation frequency in Hz (typically 38000 for NEC).
 * @property pattern Alternating on/off pulse durations in microseconds.
 */
data class IrSignal(
    val carrierFrequencyHz: Int,
    val pattern: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IrSignal) return false
        return carrierFrequencyHz == other.carrierFrequencyHz && pattern.contentEquals(other.pattern)
    }

    override fun hashCode(): Int {
        var result = carrierFrequencyHz
        result = 31 * result + pattern.contentHashCode()
        return result
    }
}

/**
 * Encodes NEC hex codes, address:command pairs, Pronto hex, or raw microsecond pulses
 * into Android ConsumerIrManager microsecond patterns.
 */
object NecIrEncoder {

    private const val DEFAULT_CARRIER_HZ = 38000
    private const val NEC_HEADER_MARK = 9000
    private const val NEC_HEADER_SPACE = 4500
    private const val NEC_BIT_MARK = 562
    private const val NEC_ONE_SPACE = 1687
    private const val NEC_ZERO_SPACE = 562

    /**
     * Parses a user-entered IR code string into an [IrSignal].
     *
     * Supported formats:
     * 1. 32-bit NEC Hex: `0x807F01FE` or `807F01FE` (MSB-first, standard LIRC/Arduino format)
     * 2. Address:Command Hex: `80:01` or `807F:01` (auto-computes ~command)
     * 3. Pronto Hex: `0000 006D 0022 0002 ...`
     * 4. Raw microsecond pulses: `9000, 4500, 562, 562, ...`
     */
    fun parse(code: String, carrierFrequencyHz: Int = DEFAULT_CARRIER_HZ): Result<IrSignal> {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("IR code cannot be empty"))
        }

        // 1. Pronto Hex (starts with "0000 ")
        if (trimmed.startsWith("0000 ", ignoreCase = true)) {
            return parseProntoHex(trimmed)
        }

        // 2. Raw microsecond pulses (contains comma or multiple spaces of digits)
        if (trimmed.contains(",") || trimmed.split(Regex("\\s+")).size > 4) {
            return parseRawPulses(trimmed, carrierFrequencyHz)
        }

        // 3. Address:Command format (e.g., "80:01" or "807F:01")
        if (trimmed.contains(":")) {
            return parseAddressCommand(trimmed, carrierFrequencyHz)
        }

        // 4. 32-bit NEC Hex (e.g., "0x807F01FE" or "807F01FE")
        val cleanHex = trimmed.removePrefix("0x").removePrefix("0X")
        if (cleanHex.matches(Regex("^[0-9a-fA-F]{1,8}$"))) {
            val value = cleanHex.toLong(16)
            return Result.success(encodeNec32Msb(value, carrierFrequencyHz))
        }

        return Result.failure(
            IllegalArgumentException(
                "Unrecognized IR format. Use 32-bit NEC hex (e.g. 0x807F01FE), addr:cmd (80:01), Pronto hex, or raw pulses."
            )
        )
    }

    /**
     * Encodes a 32-bit NEC value MSB-first (standard LIRC / IRremote sendNECMSB order).
     */
    fun encodeNec32Msb(value: Long, carrierFrequencyHz: Int = DEFAULT_CARRIER_HZ): IrSignal {
        val pulses = ArrayList<Int>(68)
        pulses.add(NEC_HEADER_MARK)
        pulses.add(NEC_HEADER_SPACE)

        for (bit in 31 downTo 0) {
            pulses.add(NEC_BIT_MARK)
            val isOne = ((value ushr bit) and 1L) == 1L
            pulses.add(if (isOne) NEC_ONE_SPACE else NEC_ZERO_SPACE)
        }

        pulses.add(NEC_BIT_MARK)
        return IrSignal(carrierFrequencyHz, pulses.toIntArray())
    }

    private fun parseAddressCommand(input: String, carrierFrequencyHz: Int): Result<IrSignal> {
        val parts = input.split(":")
        if (parts.size != 2) {
            return Result.failure(IllegalArgumentException("Use address:command format, e.g. 80:01"))
        }
        val addrHex = parts[0].trim().removePrefix("0x").removePrefix("0X")
        val cmdHex = parts[1].trim().removePrefix("0x").removePrefix("0X")

        val addr = addrHex.toIntOrNull(16)
            ?: return Result.failure(IllegalArgumentException("Invalid hex address: $addrHex"))
        val cmd = cmdHex.toIntOrNull(16)
            ?: return Result.failure(IllegalArgumentException("Invalid hex command: $cmdHex"))

        val addr16 = if (addrHex.length <= 2) {
            val invAddr = addr.inv() and 0xFF
            ((addr and 0xFF) shl 8) or invAddr
        } else {
            addr and 0xFFFF
        }

        val cmdByte = cmd and 0xFF
        val invCmd = cmdByte.inv() and 0xFF
        val full32 = (addr16.toLong() shl 16) or (cmdByte.toLong() shl 8) or invCmd.toLong()
        return Result.success(encodeNec32Msb(full32, carrierFrequencyHz))
    }

    private fun parseRawPulses(input: String, carrierFrequencyHz: Int): Result<IrSignal> {
        val tokens = input.split(Regex("[,\\s]+")).filter { it.isNotBlank() }
        val pulses = IntArray(tokens.size)
        for (i in tokens.indices) {
            val v = tokens[i].toIntOrNull()
                ?: return Result.failure(IllegalArgumentException("Invalid pulse duration: '${tokens[i]}'"))
            if (v <= 0) {
                return Result.failure(IllegalArgumentException("Pulse duration must be positive: $v"))
            }
            pulses[i] = v
        }
        if (pulses.size < 2) {
            return Result.failure(IllegalArgumentException("Raw pattern must have at least 2 pulses"))
        }
        return Result.success(IrSignal(carrierFrequencyHz, pulses))
    }

    private fun parseProntoHex(input: String): Result<IrSignal> {
        val words = input.trim().split(Regex("\\s+")).mapNotNull { it.toIntOrNull(16) }
        if (words.size < 6 || words[0] != 0) {
            return Result.failure(IllegalArgumentException("Invalid Pronto Hex header"))
        }
        val frequencyDivider = words[1]
        if (frequencyDivider <= 0) {
            return Result.failure(IllegalArgumentException("Invalid Pronto carrier divider"))
        }
        val carrierHz = (1_000_000.0 / (frequencyDivider * 0.241246)).toInt()
        val tickUs = 1_000_000.0 / carrierHz

        val oncePairCount = words[2]
        val repeatPairCount = words[3]
        val totalPairs = if (oncePairCount > 0) oncePairCount else repeatPairCount
        val startIndex = 4

        if (words.size < startIndex + totalPairs * 2) {
            return Result.failure(IllegalArgumentException("Pronto Hex data is shorter than declared burst count"))
        }

        val pulses = IntArray(totalPairs * 2)
        for (i in 0 until totalPairs * 2) {
            pulses[i] = (words[startIndex + i] * tickUs).toInt().coerceAtLeast(1)
        }
        return Result.success(IrSignal(carrierHz, pulses))
    }
}
