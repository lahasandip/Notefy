package com.sandip.notefy.ui.home

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.sandip.notefy.R
import com.sandip.notefy.data.dao.NoteDao
import com.sandip.notefy.data.dao.UserDao
import com.sandip.notefy.data.entity.NoteEntity
import com.sandip.notefy.ui.*
import com.sandip.notefy.util.PreferencesManager
import com.sandip.notefy.util.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteDao: NoteDao,
    userDao: UserDao,
    private val preferencesManager: PreferencesManager,
    state: SavedStateHandle,
    application: Application
): AndroidViewModel(application) {

    val searchQuery = state.getLiveData("searchQuery", "")
    val displayUser = userDao.getUser()

    private val preferencesFlow = preferencesManager.preferencesFlow
    val isChecked = preferencesManager.isChecked

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()
    private val app = application

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    val gridSharedPreferences : SharedPreferences =  app.getSharedPreferences("GRID", Context.MODE_PRIVATE)
    val biometricSharedPreferences : SharedPreferences =  app.getSharedPreferences("BIOMETRIC", Context.MODE_PRIVATE)
    val uiSharedPreferences : SharedPreferences = app.getSharedPreferences("UI",Context.MODE_PRIVATE)
//    val isBiometric : SharedPreferences = app.getSharedPreferences("IS_BIO",Context.MODE_PRIVATE)
    private val isFirstLaunchPreferences : SharedPreferences = app.getSharedPreferences("FIRST_LAUNCH",Context.MODE_PRIVATE)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        noteDao.getTasks(query, filterPreferences.sortOrder)
    }

    val note = tasksFlow.asLiveData()
    val noteCount = noteDao.getNotes()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }
    fun updateSortOrderIsChecked(index:Int) = viewModelScope.launch {
        preferencesManager.updateSortOrderIsChecked(index)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }
    fun onProfileClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToUserScreen)
    }

    fun onTaskSelected(noteEntity: NoteEntity) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(noteEntity))
    }
    fun onMenuTaskDelete(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.updateDao(noteEntity.copy(strike = true, isHide = true))
    }

    fun onTaskSwiped(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.updateDao(noteEntity.copy(strike = true, isHide = true))
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(noteEntity))
    }

    fun onUndoDeleteClick(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.updateDao(noteEntity.copy(isHide = false))
    }

    fun onAddEditResult(context: Context, result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage(context.getString(R.string.note_added))
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage(context.getString(R.string.note_updated))
            DELETE_TASK_RESULT_OK -> showTaskSavedConfirmationMessage(context.getString(R.string.note_deleted))
            PROFILE_UPDATED_RESULT_OK -> showTaskSavedConfirmationMessage(context.getString(R.string.profile_updated))
        }
    }

    private fun showTaskSavedConfirmationMessage(text: String) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(text))
    }

    fun onGridViewToggle(isChecked: Boolean) = viewModelScope.launch {
        val editor = gridSharedPreferences.edit()
        editor.putBoolean("grid", isChecked)
        editor.apply()
    }

    fun onScreenLockToggle(isChecked: Boolean) = viewModelScope.launch {
        val editor = biometricSharedPreferences.edit()
        editor.putBoolean("biometric", isChecked)
        editor.apply()
    }

    fun onDarkModeToggle(isChecked: Boolean) = viewModelScope.launch{
        val editor = uiSharedPreferences.edit()
        editor.putBoolean("darkMode", isChecked)
        editor.apply()
    }

    fun onScreenRotate(isFirstLaunch: Boolean) = viewModelScope.launch {
        val editor = isFirstLaunchPreferences.edit()
        editor.putBoolean("rotation", isFirstLaunch)
        editor.apply()
    }

//    fun isBiometricOn(isOn: Boolean) = viewModelScope.launch {
//        val editor = isBiometric.edit()
//        editor.putBoolean("isBiometric", isOn)
//        editor.apply()
//    }

    fun observeBiometricPreferences(mainActivity: MainActivity) = viewModelScope.launch{
            when (biometricSharedPreferences.getBoolean("biometric", false)) {
                true -> onBiometricEnabled(mainActivity)
                false -> onBiometricDisabled()
            }
        }

    private fun onBiometricEnabled(mainActivity: MainActivity) = viewModelScope.launch{
        if(isFirstLaunchPreferences.getBoolean("rotation", true)) {
            val biometricManager = this.let { BiometricManager.from(mainActivity.applicationContext) }
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {}
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {}
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {}
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    val enrollIntent =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                putExtra(
                                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                                )
                            }
                        } else {
                            Intent(Settings.ACTION_SECURITY_SETTINGS)
                        }
                    tasksEventChannel.send(TasksEvent.StartIntent(mainActivity, enrollIntent))
                }
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {}
                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {}
                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {}
            }
            executor = this.let { ContextCompat.getMainExecutor(mainActivity.applicationContext) }
            biometricPrompt = BiometricPrompt(mainActivity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(
                            mainActivity.applicationContext,
                            mainActivity.getString(R.string.auth_error, errString),
                            Toast.LENGTH_LONG
                        ).show()
                        mainActivity.finish()
                    }
                })
            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(mainActivity.getString(R.string.authenticate_with_biometric))
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
            tasksEventChannel.send(TasksEvent.AuthenticatePrompt(biometricPrompt, promptInfo))
        }
//        isBiometricOn(true)
        tasksEventChannel.send(TasksEvent.SetValue)
    }

    private fun onBiometricDisabled() = viewModelScope.launch{
        tasksEventChannel.send(TasksEvent.BiometricDisabled)
    }

    fun updateUiSwitch() = viewModelScope.launch{
        when(uiSharedPreferences.getBoolean("darkMode", false)){
            true -> tasksEventChannel.send(TasksEvent.DarkMode)
            false -> tasksEventChannel.send(TasksEvent.LightMode)
        }
    }

    sealed class TasksEvent {
        object NavigateToAddTaskScreen : TasksEvent()
        object NavigateToUserScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val noteEntity: NoteEntity) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val noteEntity: NoteEntity) : TasksEvent()
        object BiometricDisabled : TasksEvent()
        object SetValue : TasksEvent()
        object LightMode : TasksEvent()
        object DarkMode : TasksEvent()
        data class StartIntent(val mainActivity: MainActivity, val enrollIntent: Intent) : TasksEvent()
        data class AuthenticatePrompt(
            val biometricPrompt: BiometricPrompt,
            val promptInfo: BiometricPrompt.PromptInfo) : TasksEvent()
    }
}