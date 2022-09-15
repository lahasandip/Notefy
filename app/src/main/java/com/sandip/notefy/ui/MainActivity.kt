package com.sandip.notefy.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.sandip.notefy.R
import com.sandip.notefy.databinding.ActivityMainBinding
import com.sandip.notefy.ui.languages.LanguagesViewModel
import com.sandip.notefy.util.Biometric
import com.sandip.notefy.util.PreferencesManager
import com.sandip.notefy.util.UiMode
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.Executor

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()
    private val languagesViewModel: LanguagesViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var isDarkMode = true
    private var isBiometricEnable = true

    private lateinit var darkSwitch : SwitchMaterial
    private lateinit var screenLock : SwitchMaterial

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private val REQUEST_CODE = 1

    private lateinit var navController: NavController
    companion object{
        var drawerLayout: DrawerLayout? = null
        lateinit var preferencesManager : PreferencesManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        preferencesManager = PreferencesManager(applicationContext)

        observeUiPreferences()



        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Language


        val navigationView = binding.navView
        darkSwitch =
            navigationView.menu.findItem(R.id.dark_theme).actionView!!.findViewById(R.id.dark_switch)

        //Dark Mode
        darkSwitch.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> viewModel.onDarkTheme()
                false -> viewModel.onLightTheme()
            }
        }





//
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        drawerLayout = binding.drawerLayout

//        val headerView = navigationView.getHeaderView(0)
//        val profileSection = headerView.findViewById<LinearLayout>(R.id.profile_section)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fragment_home, R.id.fragment_newUpdateNote, R.id.fragment_helpfeedback,
                R.id.fragment_about,  R.id.fragment_languages,
            ), drawerLayout
        )
        navigationView.setupWithNavController(navController)


        screenLock =
            navigationView.menu.findItem(R.id.screen_lock).actionView!!.findViewById(R.id.biometric_switch)


        screenLock.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> viewModel.onScreenLockEnabled()
//                    Toast.makeText(this, "Screen Lock Enabled", Toast.LENGTH_LONG).show()
                false -> viewModel.onScreenLockDisabled()
            }
        }
//            profileSection.setOnClickListener {
//
//            }

        lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when (event) {
                    is MainActivityViewModel.MainTaskEvent.UpdateDarkUI -> {
                        Snackbar.make(view, "Dark Mode Enabled", Snackbar.LENGTH_SHORT).show()
                    }
                    is MainActivityViewModel.MainTaskEvent.UpdateLightUI -> {
                        Snackbar.make(view, "Dark Mode Disabled", Snackbar.LENGTH_SHORT).show()
                    }
//                        is MainActivityViewModel.MainTaskEvent.NavigateToMainActivity -> {
//                            val action =
//                                HomeDirections.actionHomeToNewUpdateNote()
//                            findNavController().navigate(action)
//                        }
                    else -> {}
                }.exhaustive
            }
        }

        observeBiometricPreferences()
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    private fun observeUiPreferences() {
        preferencesManager.uiModeFlow.asLiveData().observe(this) { uiMode ->
            when (uiMode) {
                UiMode.LIGHT -> onLightMode()
                UiMode.DARK -> onDarkMode()
            }
        }
    }
    private fun onLightMode() {
        isDarkMode = false
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        darkSwitch.isChecked = false


        // Actually turn on Light mode using AppCompatDelegate.setDefaultNightMode() here
    }

    private fun onDarkMode() {
        isDarkMode = true

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        darkSwitch.isChecked = true

        // Actually turn on Dark mode using AppCompatDelegate.setDefaultNightMode() here
    }
    private fun observeBiometricPreferences(){
        preferencesManager.biometricAuth.asLiveData().observe(this) { biometric ->
            when (biometric) {
                Biometric.ENABLE -> onBiometricEnabled()
                Biometric.DISABLE -> onBiometricDisabled()
            }
        }
    }
    private fun onBiometricEnabled(){
        isBiometricEnable = true
        val biometricManager = this?.let { BiometricManager.from(this) }
        when (biometricManager?.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                    )
                }
                ActivityCompat.startActivityForResult(
                    this,
                    enrollIntent,
                    REQUEST_CODE,
                    null
                )
            }
        }

//
        executor = this?.let { ContextCompat.getMainExecutor(this) }!!
        biometricPrompt = BiometricPrompt(this, executor,
            @RequiresApi(Build.VERSION_CODES.P)
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                    finish()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    )
                        .show()
//                    gotomain()

                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    finish()
                }
            })
//        Allows user to authenticate using either a Class 3 biometric or
// their lock screen credential (PIN, pattern, or password).
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            // Can't call setNegativeButtonText() and
            // setAllowedAuthenticators(... or DEVICE_CREDENTIAL) at the same time.
            // .setNegativeButtonText("Use account password")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
                        or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        screenLock.isChecked = true
        biometricPrompt.authenticate(promptInfo)
    }
    private fun onBiometricDisabled(){
        isBiometricEnable = false
        screenLock.isChecked = false


    }
}
const val ADD_TASK_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 2
