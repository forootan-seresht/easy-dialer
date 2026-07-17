package app.arteh.easydialer.utility

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.arteh.easydialer.contacts.models.SpeedDialEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class PreferencesManager(private val context: Context) {

    val SPEED_DIAL_KEY = stringPreferencesKey("speed_dial_map")
    val LangKEY = stringPreferencesKey("lang")
    val DIAL_STYLE_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("dial_style")

    suspend fun saveSpeedDial(newSlot: Int, oldSlot: Int, entry: SpeedDialEntry) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[SPEED_DIAL_KEY]
            val map = currentJson?.let { decodeMap(it) } ?: mutableMapOf()

            if (oldSlot != -1)
                map.remove(oldSlot)

            if (newSlot != -1)
                map[newSlot] = entry

            prefs[SPEED_DIAL_KEY] = encodeMap(map)
        }
    }

    fun loadSpeedDIal(): Flow<Map<Int, SpeedDialEntry>> {
        return context.dataStore.data.map { prefs ->
            prefs[SPEED_DIAL_KEY]?.let { decodeMap(it) } ?: emptyMap()
        }
    }

    fun encodeMap(map: Map<Int, SpeedDialEntry>): String {
        val root = JSONObject()

        for ((slot, entry) in map) {
            val obj = JSONObject().apply {
                put("phoneId", entry.phoneId)
                put("phoneNumber", entry.phoneNumber)
                put("displayName", entry.displayName)
            }
            root.put(slot.toString(), obj)
        }

        return root.toString()
    }

    fun decodeMap(json: String): MutableMap<Int, SpeedDialEntry> {
        val map = mutableMapOf<Int, SpeedDialEntry>()
        val root = JSONObject(json)

        val keys = root.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val obj = root.getJSONObject(key)

            val entry = SpeedDialEntry(
                phoneId = obj.getLong("phoneId"),
                phoneNumber = obj.getString("phoneNumber"),
                displayName = obj.getString("displayName")
            )

            map[key.toInt()] = entry
        }

        return map
    }

    suspend fun setLang(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[LangKEY] = lang
        }
    }

    suspend fun getLang(): String {
        val preferences = context.dataStore.data.first()
        return preferences[LangKEY] ?: ""
    }

    suspend fun setIsBigButtons(isBig: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DIAL_STYLE_KEY] = isBig
        }
    }

    fun getIsBigButtons(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[DIAL_STYLE_KEY] ?: false
        }
    }
}