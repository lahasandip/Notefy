package com.sandcastle.notefy.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
enum class SortOrder { BOOKMARKED, TITLE_ASC, TITLE_DSC, NEW_TO_OLD, OLD_TO_NEW}
data class FilterPreferences(val sortOrder: SortOrder)

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    val preferencesFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preference ->
            val sortOrder = SortOrder.valueOf(
                preference[PreferencesKeys.SORT_ORDER] ?: SortOrder.TITLE_ASC.name
            )
            FilterPreferences(sortOrder)
        }

    val isChecked = dataStore.data
        .catch {exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preference ->
            val isChecked = preference[PreferencesKeys.SORT_ORDER_CHECKED] ?: 3
            isChecked
        }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateSortOrderIsChecked(isChecked: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER_CHECKED] = isChecked        }
    }

    private object PreferencesKeys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val SORT_ORDER_CHECKED = intPreferencesKey("sort_order_checked")
    }
}