package com.fenda.x200feremote.ir

import android.content.Context
import android.hardware.ConsumerIrManager

sealed interface TransmitResult {
    data class Success(val carrierHz: Int, val pulseCount: Int) : TransmitResult
    data class NoHardware(val message: String) : TransmitResult
    data class Error(val message: String) : TransmitResult
}

/**
 * Thin wrapper around Android's [ConsumerIrManager] for transmitting IR signals.
 */
class IrTransmitter(context: Context) {

    private val irManager: ConsumerIrManager? =
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    val hasIrEmitter: Boolean
        get() = irManager?.hasIrEmitter() == true

    fun supportedFrequenciesSummary(): String {
        val ranges = irManager?.carrierFrequencies ?: return "No IR hardware detected"
        if (ranges.isEmpty()) return "No carrier ranges reported"
        return ranges.joinToString(", ") { "${it.minFrequency / 1000}-${it.maxFrequency / 1000} kHz" }
    }

    fun transmit(code: String, carrierFrequencyHz: Int = 38000): TransmitResult {
        val signal = NecIrEncoder.parse(code, carrierFrequencyHz).getOrElse { err ->
            return TransmitResult.Error(err.message ?: "Invalid IR code")
        }

        val manager = irManager
        if (manager == null || !manager.hasIrEmitter()) {
            return TransmitResult.NoHardware(
                "No built-in IR blaster detected on this device (simulated ${signal.pattern.size} pulses @ ${signal.carrierFrequencyHz / 1000} kHz)"
            )
        }

        return try {
            manager.transmit(signal.carrierFrequencyHz, signal.pattern)
            TransmitResult.Success(signal.carrierFrequencyHz, signal.pattern.size)
        } catch (e: Exception) {
            TransmitResult.Error(e.message ?: "Failed to transmit IR signal")
        }
    }
}
