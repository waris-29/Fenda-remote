package com.fenda.x200feremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.SettingsInputComponent
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fenda.x200feremote.data.RemoteCodeStore
import com.fenda.x200feremote.ir.IrTransmitter
import com.fenda.x200feremote.ir.TransmitResult
import com.fenda.x200feremote.model.ButtonCategory
import com.fenda.x200feremote.model.RemoteButton
import com.fenda.x200feremote.ui.components.ColorSwatchButton
import com.fenda.x200feremote.ui.components.EditIrCodeDialog
import com.fenda.x200feremote.ui.components.ProfileJsonDialog
import com.fenda.x200feremote.ui.components.RemoteActionButton
import com.fenda.x200feremote.ui.components.SectionCard
import com.fenda.x200feremote.ui.components.TopPowerBar
import com.fenda.x200feremote.ui.theme.FendaRemoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val transmitter = IrTransmitter(this)
        val store = RemoteCodeStore(this)

        setContent {
            FendaRemoteTheme {
                FendaRemoteScreen(transmitter = transmitter, store = store)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FendaRemoteScreen(
    transmitter: IrTransmitter,
    store: RemoteCodeStore
) {
    val haptics = LocalHapticFeedback.current
    var buttons by remember { mutableStateOf(store.loadButtons()) }
    var editMode by remember { mutableStateOf(false) }
    var selectedColorId by remember { mutableStateOf<String?>(null) }
    var editingButton by remember { mutableStateOf<RemoteButton?>(null) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var statusMessage by remember {
        mutableStateOf(
            if (transmitter.hasIrEmitter) {
                "IR Blaster ready (${transmitter.supportedFrequenciesSummary()})"
            } else {
                "No IR Blaster detected — running in preview/editor mode"
            }
        )
    }

    val byId = remember(buttons) { buttons.associateBy { it.id } }
    fun btn(id: String): RemoteButton = byId.getValue(id)

    fun fireButton(button: RemoteButton) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        if (button.category == ButtonCategory.LIGHT_COLOR) {
            selectedColorId = button.id
        }
        statusMessage = when (val result = transmitter.transmit(button.irCode, button.carrierHz)) {
            is TransmitResult.Success ->
                "Sent ${button.label} (${button.irCode}) • ${result.pulseCount} pulses @ ${result.carrierHz / 1000} kHz"
            is TransmitResult.NoHardware ->
                "Preview ${button.label} (${button.irCode}) — ${result.message}"
            is TransmitResult.Error ->
                "Error sending ${button.label}: ${result.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Fenda X200FE",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (transmitter.hasIrEmitter) Color(0xFF34C759)
                                        else Color(0xFFF59E0B)
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (transmitter.hasIrEmitter) "IR Blaster Ready" else "No IR Hardware",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    FilterChip(
                        selected = editMode,
                        onClick = { editMode = !editMode },
                        label = { Text(if (editMode) "Done" else "Edit Codes") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    IconButton(onClick = { showProfileDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Code,
                            contentDescription = "Import/Export JSON"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (editMode) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Edit Mode active: tap any button to customize its NEC Hex, addr:cmd, Pronto Hex, or raw pulses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // 1. Power, Light Power, Mute
            TopPowerBar(
                powerBtn = btn("power"),
                muteBtn = btn("mute"),
                lightPowerBtn = btn("light_power"),
                editMode = editMode,
                onPress = ::fireButton,
                onEdit = { editingButton = it }
            )

            // 2. Lights & Colors
            SectionCard(
                title = "Lights & Colors",
                subtitle = "Long-press any button to edit",
                accentColor = MaterialTheme.colorScheme.secondary
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RemoteActionButton(
                        button = btn("light_mode"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        modifier = Modifier.weight(1.2f)
                    )
                    RemoteActionButton(
                        button = btn("light_bright_down"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        modifier = Modifier.weight(1f)
                    )
                    RemoteActionButton(
                        button = btn("light_bright_up"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val colorButtons = buttons.filter { it.category == ButtonCategory.LIGHT_COLOR }
                colorButtons.chunked(4).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowItems.forEach { swatchBtn ->
                            ColorSwatchButton(
                                button = swatchBtn,
                                isSelected = selectedColorId == swatchBtn.id,
                                editMode = editMode,
                                onPress = ::fireButton,
                                onEdit = { editingButton = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val fxButtons = buttons.filter { it.category == ButtonCategory.LIGHT_EFFECT }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fxButtons.forEach { fxBtn ->
                        RemoteActionButton(
                            button = fxBtn,
                            editMode = editMode,
                            onPress = ::fireButton,
                            onEdit = { editingButton = it },
                            containerColor = Color(0xFF231934),
                            contentColor = Color(0xFFE9D5FF),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Input / Source
            SectionCard(
                title = "Input Source",
                accentColor = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RemoteActionButton(
                        button = btn("source_mode"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    RemoteActionButton(
                        button = btn("input_bt"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        icon = Icons.Rounded.Bluetooth,
                        modifier = Modifier.weight(1f)
                    )
                    RemoteActionButton(
                        button = btn("input_aux"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        icon = Icons.Rounded.SettingsInputComponent,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RemoteActionButton(
                        button = btn("input_usb"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        icon = Icons.Rounded.Usb,
                        modifier = Modifier.weight(1f)
                    )
                    RemoteActionButton(
                        button = btn("input_opt"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        modifier = Modifier.weight(1f)
                    )
                    RemoteActionButton(
                        button = btn("input_fm"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        icon = Icons.Rounded.Radio,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Audio & Tone
            SectionCard(
                title = "Audio & Tone",
                accentColor = Color(0xFF34C759)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RemoteActionButton(
                        button = btn("vol_down"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        icon = Icons.Rounded.Remove,
                        modifier = Modifier.weight(1f)
                    )
                    RemoteActionButton(
                        button = btn("vol_up"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        icon = Icons.Rounded.Add,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    RemoteActionButton(
                        button = btn("eq_preset"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        icon = Icons.Rounded.GraphicEq,
                        modifier = Modifier.weight(1f)
                    )
                    RemoteActionButton(
                        button = btn("reset_audio"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RemoteActionButton(
                        button = btn("bass_down"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        modifier = Modifier.weight(1f)
                    )
                    RemoteActionButton(
                        button = btn("bass_up"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        modifier = Modifier.weight(1f)
                    )
                    RemoteActionButton(
                        button = btn("treble_down"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        modifier = Modifier.weight(1f)
                    )
                    RemoteActionButton(
                        button = btn("treble_up"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 5. Playback & FM Tuner
            SectionCard(
                title = "Playback & FM Tuner",
                accentColor = Color(0xFFF59E0B)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RemoteActionButton(
                        button = btn("prev_track"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        icon = Icons.Rounded.FastRewind,
                        modifier = Modifier.weight(1f)
                    )
                    RemoteActionButton(
                        button = btn("play_pause"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        icon = Icons.Rounded.PlayArrow,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1.3f)
                    )
                    RemoteActionButton(
                        button = btn("next_track"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        icon = Icons.Rounded.FastForward,
                        modifier = Modifier.weight(1f)
                    )
                    RemoteActionButton(
                        button = btn("repeat"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        icon = Icons.Rounded.Repeat,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RemoteActionButton(
                        button = btn("ch_prev"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        modifier = Modifier.weight(1f)
                    )
                    RemoteActionButton(
                        button = btn("fm_scan"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        modifier = Modifier.weight(1.3f)
                    )
                    RemoteActionButton(
                        button = btn("ch_next"),
                        editMode = editMode,
                        onPress = ::fireButton,
                        onEdit = { editingButton = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 6. Numpad
            SectionCard(
                title = "Direct Track / Station",
                accentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                val numpadRows = listOf(
                    listOf("num_1", "num_2", "num_3"),
                    listOf("num_4", "num_5", "num_6"),
                    listOf("num_7", "num_8", "num_9"),
                    listOf("num_0")
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    numpadRows.forEach { rowIds ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowIds.forEach { id ->
                                RemoteActionButton(
                                    button = btn(id),
                                    editMode = editMode,
                                    onPress = ::fireButton,
                                    onEdit = { editingButton = it },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    editingButton?.let { target ->
        EditIrCodeDialog(
            button = target,
            onDismiss = { editingButton = null },
            onTestFire = { code, hz ->
                val res = transmitter.transmit(code, hz)
                statusMessage = when (res) {
                    is TransmitResult.Success -> "Test fired $code @ ${hz / 1000} kHz"
                    is TransmitResult.NoHardware -> "Test preview $code — ${res.message}"
                    is TransmitResult.Error -> "Test error: ${res.message}"
                }
            },
            onSave = { newLabel, newCode, newHz ->
                store.saveButtonOverride(target.id, newLabel, newCode, newHz)
                buttons = store.loadButtons()
                editingButton = null
                statusMessage = "Saved ${newLabel} ($newCode)"
            }
        )
    }

    if (showProfileDialog) {
        ProfileJsonDialog(
            exportedJson = store.exportProfileJson(buttons),
            onDismiss = { showProfileDialog = false },
            onImport = { json ->
                store.importProfileJson(json).onSuccess { count ->
                    buttons = store.loadButtons()
                    showProfileDialog = false
                    statusMessage = "Imported $count button codes"
                }.onFailure { err ->
                    statusMessage = "Import failed: ${err.message}"
                }
            },
            onResetDefaults = {
                store.resetToDefaults()
                buttons = store.loadButtons()
                showProfileDialog = false
                statusMessage = "Reset all buttons to default NEC codes"
            }
        )
    }
}
