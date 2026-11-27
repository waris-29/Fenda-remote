package com.fenda.x200feremote.data

import android.content.Context
import com.fenda.x200feremote.model.DefaultRemoteProfile
import com.fenda.x200feremote.model.RemoteButton
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists custom IR code mappings per button in SharedPreferences and supports
 * JSON profile export/import.
 */
class RemoteCodeStore(context: Context) {

    private val prefs = context.getSharedPreferences("fenda_x200fe_remote_codes", Context.MODE_PRIVATE)

    fun loadButtons(): List<RemoteButton> {
        val raw = prefs.getString(KEY_OVERRIDES, null) ?: return DefaultRemoteProfile.buttons
        val overrides = parseOverrides(raw)
        return DefaultRemoteProfile.buttons.map { base ->
            val custom = overrides[base.id]
            if (custom != null) {
                base.copy(
                    label = custom.label ?: base.label,
                    irCode = custom.irCode ?: base.irCode,
                    carrierHz = custom.carrierHz ?: base.carrierHz
                )
            } else {
                base
            }
        }
    }

    fun saveButtonOverride(buttonId: String, label: String, irCode: String, carrierHz: Int) {
        val current = parseOverrides(prefs.getString(KEY_OVERRIDES, null)).toMutableMap()
        current[buttonId] = ButtonOverride(label = label, irCode = irCode, carrierHz = carrierHz)
        writeOverrides(current)
    }

    fun resetToDefaults() {
        prefs.edit().remove(KEY_OVERRIDES).apply()
    }

    fun exportProfileJson(buttons: List<RemoteButton>): String {
        val root = JSONObject()
        root.put("model", "Fenda X200FE")
        root.put("version", 1)
        val arr = JSONArray()
        for (btn in buttons) {
            arr.put(
                JSONObject().apply {
                    put("id", btn.id)
                    put("label", btn.label)
                    put("irCode", btn.irCode)
                    put("carrierHz", btn.carrierHz)
                }
            )
        }
        root.put("buttons", arr)
        return root.toString(2)
    }

    fun importProfileJson(jsonText: String): Result<Int> {
        return try {
            val root = JSONObject(jsonText)
            val arr = root.getJSONArray("buttons")
            val validIds = DefaultRemoteProfile.buttons.map { it.id }.toSet()
            val overrides = mutableMapOf<String, ButtonOverride>()
            var updatedCount = 0

            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.getString("id")
                if (id in validIds) {
                    overrides[id] = ButtonOverride(
                        label = obj.optString("label").takeIf { it.isNotBlank() },
                        irCode = obj.optString("irCode").takeIf { it.isNotBlank() },
                        carrierHz = if (obj.has("carrierHz")) obj.optInt("carrierHz", 38000) else null
                    )
                    updatedCount++
                }
            }
            writeOverrides(overrides)
            Result.success(updatedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class ButtonOverride(
        val label: String?,
        val irCode: String?,
        val carrierHz: Int?
    )

    private fun parseOverrides(jsonStr: String?): Map<String, ButtonOverride> {
        if (jsonStr.isNullOrBlank()) return emptyMap()
        return try {
            val root = JSONObject(jsonStr)
            val map = mutableMapOf<String, ButtonOverride>()
            val keys = root.keys()
            while (keys.hasNext()) {
                val id = keys.next()
                val obj = root.getJSONObject(id)
                map[id] = ButtonOverride(
                    label = obj.optString("label").takeIf { it.isNotBlank() },
                    irCode = obj.optString("irCode").takeIf { it.isNotBlank() },
                    carrierHz = if (obj.has("carrierHz")) obj.optInt("carrierHz", 38000) else null
                )
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun writeOverrides(overrides: Map<String, ButtonOverride>) {
        val root = JSONObject()
        for ((id, ov) in overrides) {
            val obj = JSONObject()
            ov.label?.let { obj.put("label", it) }
            ov.irCode?.let { obj.put("irCode", it) }
            ov.carrierHz?.let { obj.put("carrierHz", it) }
            root.put(id, obj)
        }
        prefs.edit().putString(KEY_OVERRIDES, root.toString()).apply()
    }

    companion object {
        private const val KEY_OVERRIDES = "button_overrides_json"
    }
}
