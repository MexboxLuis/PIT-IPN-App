package com.example.pitapp.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.pitapp.ui.model.PreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesManager(private val context: Context) {

    private val isGridViewKey = booleanPreferencesKey("is_grid_view")

    val isGridView: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_GRID_VIEW] ?: false
    }

    suspend fun setIsGridView(isGrid: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[isGridViewKey] = isGrid
        }
    }

}



