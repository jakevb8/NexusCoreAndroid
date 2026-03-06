package me.jakev.nexuscore.data.api

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "nexus_prefs")

enum class BackendChoice(val baseUrl: String, val label: String) {
    JS("https://nexus-coreapi-production.up.railway.app/api/v1/", "NexusCoreJS (.NET API)"),
    DOTNET("https://nexuscoredotnet-production.up.railway.app/api/v1/", "NexusCoreDotNet")
}

@Singleton
class BackendPreference @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val BACKEND_KEY = stringPreferencesKey("selected_backend")

    val flow = context.dataStore.data.map { prefs ->
        val saved = prefs[BACKEND_KEY] ?: BackendChoice.JS.name
        BackendChoice.valueOf(saved)
    }

    suspend fun get(): BackendChoice = flow.first()

    suspend fun set(choice: BackendChoice) {
        context.dataStore.updateData { prefs ->
            prefs.toMutablePreferences().apply { set(BACKEND_KEY, choice.name) }
        }
    }
}
