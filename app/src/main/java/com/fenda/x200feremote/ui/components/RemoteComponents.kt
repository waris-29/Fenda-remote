package com.fenda.x200feremote.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fenda.x200feremote.ir.NecIrEncoder
import com.fenda.x200feremote.model.RemoteButton

@Composable
fun SectionCard(
    title: String,
    subtitle: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            content()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemoteActionButton(
    button: RemoteButton,
    editMode: Boolean,
    onPress: (RemoteButton) -> Unit,
    onEdit: (RemoteButton) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val borderColor = if (editMode) {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = { if (editMode) onEdit(button) else onPress(button) },
                onLongClick = { onEdit(button) }
            ),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = button.label,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = button.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                if (editMode) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = button.irCode.take(10),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1
                    )
                }
            }
            if (editMode) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(12.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorSwatchButton(
    button: RemoteButton,
    isSelected: Boolean,
    editMode: Boolean,
    onPress: (RemoteButton) -> Unit,
    onEdit: (RemoteButton) -> Unit,
    modifier: Modifier = Modifier
) {
    val swatch = button.swatchColor ?: MaterialTheme.colorScheme.primary
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = { if (editMode) onEdit(button) else onPress(button) },
                onLongClick = { onEdit(button) }
            )
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(swatch)
                .border(
                    width = if (isSelected || editMode) 2.5.dp else 1.dp,
                    color = when {
                        editMode -> MaterialTheme.colorScheme.secondary
                        isSelected -> Color.White
                        else -> Color.White.copy(alpha = 0.25f)
                    },
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = button.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TopPowerBar(
    powerBtn: RemoteButton,
    muteBtn: RemoteButton,
    lightPowerBtn: RemoteButton,
    editMode: Boolean,
    onPress: (RemoteButton) -> Unit,
    onEdit: (RemoteButton) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RemoteActionButton(
            button = powerBtn,
            editMode = editMode,
            onPress = onPress,
            onEdit = onEdit,
            icon = Icons.Rounded.PowerSettingsNew,
            containerColor = Color(0xFF3A141E),
            contentColor = Color(0xFFFB7185),
            modifier = Modifier.weight(1f)
        )
        RemoteActionButton(
            button = lightPowerBtn,
            editMode = editMode,
            onPress = onPress,
            onEdit = onEdit,
            icon = Icons.Rounded.Lightbulb,
            containerColor = Color(0xFF281A3D),
            contentColor = Color(0xFFD8B4FE),
            modifier = Modifier.weight(1f)
        )
        RemoteActionButton(
            button = muteBtn,
            editMode = editMode,
            onPress = onPress,
            onEdit = onEdit,
            icon = Icons.Rounded.VolumeOff,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun EditIrCodeDialog(
    button: RemoteButton,
    onDismiss: () -> Unit,
    onTestFire: (code: String, carrierHz: Int) -> Unit,
    onSave: (label: String, code: String, carrierHz: Int) -> Unit
) {
    var label by remember(button.id) { mutableStateOf(button.label) }
    var code by remember(button.id) { mutableStateOf(button.irCode) }
    var carrierText by remember(button.id) { mutableStateOf(button.carrierHz.toString()) }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun validate(): Pair<String, Int>? {
        val hz = carrierText.toIntOrNull()
        if (hz == null || hz !in 10_000..100_000) {
            validationError = "Carrier frequency must be between 10000 and 100000 Hz"
            return null
        }
        val parseResult = NecIrEncoder.parse(code, hz)
        if (parseResult.isFailure) {
            validationError = parseResult.exceptionOrNull()?.message ?: "Invalid IR code"
            return null
        }
        validationError = null
        return code.trim() to hz
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit IR Code (${button.id})") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Button Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        validationError = null
                    },
                    label = { Text("IR Code (NEC Hex / addr:cmd / Pronto / Raw)") },
                    placeholder = { Text("0x807F01FE") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
                OutlinedTextField(
                    value = carrierText,
                    onValueChange = { carrierText = it },
                    label = { Text("Carrier Frequency (Hz)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Tip: Accepts 32-bit NEC hex (0x807F01FE), address:command (80:01), Pronto hex (0000 ...), or raw microsecond pulses.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val valid = validate() ?: return@Button
                    onSave(label.trim().ifEmpty { button.label }, valid.first, valid.second)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        val valid = validate() ?: return@OutlinedButton
                        onTestFire(valid.first, valid.second)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Test Fire")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun ProfileJsonDialog(
    exportedJson: String,
    onDismiss: () -> Unit,
    onImport: (String) -> Unit,
    onResetDefaults: () -> Unit
) {
    var jsonInput by remember(exportedJson) { mutableStateOf(exportedJson) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remote Profile JSON") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Copy this JSON to back up your Fenda X200FE codes, or paste a profile JSON below and tap Import.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = { jsonInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onImport(jsonInput) }) {
                Text("Import")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onResetDefaults) {
                    Text("Reset Defaults", color = MaterialTheme.colorScheme.tertiary)
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}
