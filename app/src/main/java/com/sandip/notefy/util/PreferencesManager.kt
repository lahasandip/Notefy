package com.sandip.notefy.util

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
private const val TAG = "PreferencesManager"

enum class SortOrder { BOOKMARKED, TITLE_ASC, TITLE_DSC, NEW_TO_OLD, OLD_TO_NEW}
enum class UiMode { LIGHT, DARK }
//enum class Biometric { ENABLE, DISABLE }

data class FilterPreferences(val sortOrder: SortOrder)

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {
    companion object{
        val IS_DARK_MODE = booleanPreferencesKey("dark_mode")
//        val IS_BIOMETRIC_ENABLE = booleanPreferencesKey("biometric")
//        val LANGUAGE = intPreferencesKey("language")
    }
    private val dataStore = context.dataStore

    val preferencesFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preference1 ->
            val sortOrder = SortOrder.valueOf(
                preference1[PreferencesKeys.SORT_ORDER] ?: SortOrder.TITLE_ASC.name
            )

            FilterPreferences(sortOrder)

        }
    val uiModeFlow = dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preference2 ->
            val isDarkMode = preference2[IS_DARK_MODE] ?: false

            when (isDarkMode) {
                true -> UiMode.DARK
                false -> UiMode.LIGHT
            }
        }
//    val biometricAuth: Flow<Biometric> = dataStore.data
//        .catch {
//            if (it is IOException) {
//                it.printStackTrace()
//                emit(emptyPreferences())
//            } else {
//                throw it
//            }
//        }
//        .map { preference3 ->
//            val isBiometricEnable = preference3[IS_BIOMETRIC_ENABLE] ?: false
//
//            when (isBiometricEnable) {
//                true -> Biometric.ENABLE
//                false -> Biometric.DISABLE
//            }
//        }
//    val languageCode: Flow<Int> = dataStore.data
//        .catch {
//            if (it is IOException) {
//                it.printStackTrace()
//                emit(emptyPreferences())
//            } else {
//                throw it
//            }
//        }
//        .map { preference4 ->
//            val language = preference4[LANGUAGE] ?: 0
//            language
//        }
    val isChecked = dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preference2 ->
            val isChecked = preference2[PreferencesKeys.SORT_ORDER_CHECKED] ?: 3
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

    suspend fun setUiMode(uiMode: UiMode) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = when (uiMode) {
                UiMode.LIGHT -> false
                UiMode.DARK -> true
            }
        }
    }
//    suspend fun setBiometric(biometric: Biometric) {
//        dataStore.edit { preferences ->
//            preferences[IS_BIOMETRIC_ENABLE] = when (biometric) {
//                Biometric.DISABLE -> false
//                Biometric.ENABLE -> true
//            }
//        }
//    }
//    suspend fun storeLocale(language: Int) {
//        dataStore.edit { preferences ->
//            preferences[LANGUAGE] = language
//        }
//    }


    private object PreferencesKeys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val SORT_ORDER_CHECKED = intPreferencesKey("sort_order_checked")


    }
}