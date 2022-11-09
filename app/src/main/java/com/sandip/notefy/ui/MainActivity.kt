package com.sandip.notefy.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.sandip.notefy.R
import com.sandip.notefy.databinding.ActivityMainBinding
import com.sandip.notefy.ui.home.HomeViewModel
import com.sandip.notefy.util.ModeManager.observeLanguagePreference
import com.sandip.notefy.util.ModeManager.observeUiPreferences
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor

@Suppress("IMPLICIT_CAST_TO_ANY")
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener{

    private var binding: ActivityMainBinding? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var darkSwitch : SwitchMaterial
    private lateinit var screenLock : SwitchMaterial
    private lateinit var navigationView : NavigationView
    private val viewModel : HomeViewModel by viewModels()
    private val requestCode = 1
    private var isDarkMode = true
    private var isBiometricEnable = true
    private var drawerLayout: DrawerLayout? = null
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        observeUiPreferences(applicationContext)
        observeLanguagePreference(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        viewModel.uiSharedPreferences.registerOnSharedPreferenceChangeListener(this)
        viewModel.biometricSharedPreferences.registerOnSharedPreferenceChangeListener(this)

        navigationView = binding!!.navView
        darkSwitch = navigationView.menu.findItem(R.id.dark_theme).actionView!!.findViewById(R.id.dark_switch)
        screenLock = navigationView.menu.findItem(R.id.screen_lock).actionView!!.findViewById(R.id.biometric_switch)

        val headerView = navigationView.getHeaderView(0)
        val userPhoto = headerView.findViewById<ImageView>(R.id.user_photo2)
        val userName = headerView.findViewById<TextView>(R.id.user_name)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        drawerLayout = binding!!.drawerLayout

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fragment_home, R.id.fragment_newUpdateNote, R.id.fragment_help_feedback,
                R.id.fragment_about,  R.id.fragment_languages, R.id.fragment_user
            ), drawerLayout
        )
        navigationView.setupWithNavController(navController)

        viewModel.displayUser.observe(this) {
            if(it !=null){
                userName.apply {
                    text = it.name
                    visibility = when (text) {
                        "" -> View.GONE
                        else -> View.VISIBLE
                    }
                }
                if(!(it.image.isNullOrEmpty())){
                    Glide.with(this).load(Uri.parse(it.image)).into(userPhoto)
                }
            }}

        darkSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onDarkModeToggle(isChecked)
        }

        screenLock.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onScreenLockToggle(isChecked)
        }

        observeBiometricPreferences()
        viewModel.updateUiSwitch()

        this.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                when (event) {
                    is HomeViewModel.TasksEvent.LightMode -> {
                        isDarkMode = false
                        darkSwitch.isChecked = false
                    }
                    is HomeViewModel.TasksEvent.DarkMode -> {
                        isDarkMode =true
                        darkSwitch.isChecked = true
                    }
                    else -> {}
                }.exhaustive
            }
        }
    }
    private fun observeBiometricPreferences(){
        when (viewModel.biometricSharedPreferences.getBoolean("biometric", false)) {
            true -> onBiometricEnabled()
            false -> onBiometricDisabled()
        }
    }

    private fun onBiometricEnabled(){
        if(viewModel.isFirstLaunchPreferences.getBoolean("rotation", true)) {
            val biometricManager = this.let { BiometricManager.from(applicationContext) }
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
                    ActivityCompat.startActivityForResult(
                        this,
                        enrollIntent,
                        requestCode,
                        null
                    )
                }
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {}
                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {}
                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {}
            }
            executor = ContextCompat.getMainExecutor(applicationContext)
            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.auth_error, errString),
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                })
            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.authenticate_with_biometric))
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
            biometricPrompt.authenticate(promptInfo)
        }
//        isBiometricOn(true)
        isBiometricEnable = true
        screenLock.isChecked = true    }

    private fun onBiometricDisabled() {
        isBiometricEnable = false
        screenLock.isChecked = false
        viewModel.onScreenRotate(true)    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if(screenLock.isChecked) {
            viewModel.onScreenRotate(false)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key.equals("darkMode"))  {
            observeUiPreferences(applicationContext)
        }
        if(key.equals("biometric"))  {
            observeBiometricPreferences()
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.onScreenRotate(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        drawerLayout = null
        binding = null
        viewModel.uiSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        viewModel.biometricSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}

const val ADD_TASK_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val PROFILE_UPDATED_RESULT_OK = Activity.RESULT_FIRST_USER + 3
const val CHANNEL_ID: String = "4"
const val CHANNEL_NAME: String = "Notefy"
const val CHANNEL_DESCRIPTION = "Reminder Message"
const val CAMERA = 0
const val GALLERY = 1



