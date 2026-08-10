package com.echocall.lab.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.echocall.lab.model.PendingProcessingMarker
import kotlinx.coroutines.flow.first

private val Context.pendingProcessingDataStore by preferencesDataStore(
    name = "pending_processing",
)

class PendingProcessingStore(context: Context) {
    private val dataStore: DataStore<Preferences> =
        context.applicationContext.pendingProcessingDataStore

    suspend fun markPending(marker: PendingProcessingMarker) {
        dataStore.edit { preferences ->
            preferences[PENDING] = true
            preferences[SCENARIO_ID] = marker.scenarioId
            preferences[VARIANT] = marker.variant
            preferences[PACKET_LENGTH] = marker.packetLength
            preferences[TIMESTAMP] = marker.timestamp
            preferences[SOURCE] = marker.source
        }
    }

    suspend fun clearPending() {
        dataStore.edit { preferences ->
            preferences.remove(PENDING)
            preferences.remove(SCENARIO_ID)
            preferences.remove(VARIANT)
            preferences.remove(PACKET_LENGTH)
            preferences.remove(TIMESTAMP)
            preferences.remove(SOURCE)
        }
    }

    suspend fun readPending(): PendingProcessingMarker? {
        val preferences = dataStore.data.first()
        if (preferences[PENDING] != true) {
            return null
        }

        return PendingProcessingMarker(
            scenarioId = preferences.required(SCENARIO_ID),
            variant = preferences.required(VARIANT),
            packetLength = preferences.required(PACKET_LENGTH),
            timestamp = preferences.required(TIMESTAMP),
            source = preferences.required(SOURCE),
        )
    }

    private fun <T> Preferences.required(key: Preferences.Key<T>): T =
        requireNotNull(this[key]) {
            "Pending processing marker is incomplete: ${key.name}"
        }

    private companion object {
        val PENDING = booleanPreferencesKey("pending")
        val SCENARIO_ID = stringPreferencesKey("scenario_id")
        val VARIANT = stringPreferencesKey("variant")
        val PACKET_LENGTH = intPreferencesKey("packet_length")
        val TIMESTAMP = stringPreferencesKey("timestamp")
        val SOURCE = stringPreferencesKey("source")
    }
}
