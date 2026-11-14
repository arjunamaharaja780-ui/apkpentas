package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.DefaultData
import com.example.model.UserRole
import com.example.model.UserSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.pentasDataStore: DataStore<Preferences> by preferencesDataStore(name = "pentas_pai_prefs")

class SessionDataStore(private val context: Context) {
    private val keyDbUrl = stringPreferencesKey("supabase_url")
    private val keyDbAnonKey = stringPreferencesKey("supabase_anon_key")
    private val keySessionJson = stringPreferencesKey("user_session_json")

    suspend fun getDbConfig(): Pair<String, String> {
        val prefs = context.pentasDataStore.data.first()
        val url = prefs[keyDbUrl]?.takeIf { it.isNotBlank() } ?: DefaultData.DEFAULT_SUPABASE_URL
        val key = prefs[keyDbAnonKey]?.takeIf { it.isNotBlank() } ?: DefaultData.DEFAULT_SUPABASE_ANON_KEY
        return url to key
    }

    suspend fun saveDbConfig(url: String, anonKey: String) {
        context.pentasDataStore.edit { prefs ->
            prefs[keyDbUrl] = url.trim()
            prefs[keyDbAnonKey] = anonKey.trim()
        }
    }

    suspend fun clearDbConfig() {
        context.pentasDataStore.edit { prefs ->
            prefs.remove(keyDbUrl)
            prefs.remove(keyDbAnonKey)
        }
    }

    suspend fun getSession(): UserSession? {
        val raw = context.pentasDataStore.data.map { it[keySessionJson] }.first() ?: return null
        return try {
            val obj = JSONObject(raw)
            val role = UserRole.fromKey(obj.optString("role")) ?: return null
            UserSession(
                role = role,
                schoolName = obj.optString("schoolName").takeIf { it.isNotBlank() && it != "null" },
                juriLomba = obj.optString("juriLomba").takeIf { it.isNotBlank() && it != "null" },
                juriKe = obj.optInt("juriKe", 0).takeIf { it > 0 },
                juriName = obj.optString("juriName").takeIf { it.isNotBlank() && it != "null" }
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveSession(session: UserSession) {
        val obj = JSONObject().apply {
            put("role", session.role.key)
            put("schoolName", session.schoolName ?: "")
            put("juriLomba", session.juriLomba ?: "")
            put("juriKe", session.juriKe ?: 0)
            put("juriName", session.juriName ?: "")
        }
        context.pentasDataStore.edit { prefs ->
            prefs[keySessionJson] = obj.toString()
        }
    }

    suspend fun clearSession() {
        context.pentasDataStore.edit { prefs ->
            prefs.remove(keySessionJson)
        }
    }
}
