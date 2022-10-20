package com.sandip.notefy.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.switchmaterial.SwitchMaterial
import com.sandip.notefy.R
import com.sandip.notefy.databinding.ActivityMainBinding
import com.sandip.notefy.ui.home.NoteAdapter.Companion.homeActionMode
import com.sandip.notefy.ui.recycle_bin.RecycleAdapter.Companion.recycleActionMode
import com.sandip.notefy.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener{
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var isDarkMode = true
    private var isBiometricEnable = true
    private lateinit var uiSharedPreferences: SharedPreferences
    private lateinit var biometricSharedPreferences: SharedPreferences


    private lateinit var darkSwitch : SwitchMaterial
    private lateinit var screenLock : SwitchMaterial

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private val requestCode = 1

    private lateinit var navController: NavController
    companion object{
        var drawerLayout: DrawerLayout? = null
        lateinit var preferencesManager : PreferencesManager

    }
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        preferencesManager = PreferencesManager(applicationContext)

        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        uiSharedPreferences =  getSharedPreferences("UI",Context.MODE_PRIVATE)
        biometricSharedPreferences =  getSharedPreferences("BIOMETRIC",Context.MODE_PRIVATE)


        //Language
        val navigationView = binding.navView
        darkSwitch = navigationView.menu.findItem(R.id.dark_theme).actionView!!.findViewById(R.id.dark_switch)
        darkSwitch.setOnCheckedChangeListener { _, isChecked ->
            val editor = uiSharedPreferences.edit()
            editor.putBoolean("darkMode",isChecked)
            editor.apply()
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        drawerLayout = binding.drawerLayout

        val headerView = navigationView.getHeaderView(0)
        val userPhoto = headerView.findViewById<ImageView>(R.id.user_photo2)
        val userName = headerView.findViewById<TextView>(R.id.user_name)
        viewModel.displayUser.observe(this) {
            if(it !=null){
                userName.text = it.name
                when(userName.text){
                    "" -> userName.visibility = View.GONE
                    else -> userName.visibility = View.VISIBLE
                }
                if(!(it.image.isNullOrEmpty())){
                    val imageUri = Uri.parse(it.image)
                    this.let { it1 -> Glide.with(it1).load(imageUri).into(userPhoto) }
                }
            }}

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fragment_home, R.id.fragment_newUpdateNote, R.id.fragment_help_feedback,
                R.id.fragment_about,  R.id.fragment_languages, R.id.fragment_user
            ), drawerLayout
        )
        navigationView.setupWithNavController(navController)


        screenLock = navigationView.menu.findItem(R.id.screen_lock).actionView!!.findViewById(R.id.biometric_switch)
        screenLock.setOnCheckedChangeListener { _, isChecked ->
            val editor = biometricSharedPreferences.edit()
            editor.putBoolean("biometric",isChecked)
            editor.apply()
        }

//        viewModel.langPosition.asLiveData().observe(this) {
//            viewModel.onTaskSelected(this, it)
//        }

        drawerLayout?.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }
            override fun onDrawerOpened(drawerView: View) {
            }
            override fun onDrawerClosed(drawerView: View) {
            }
            override fun onDrawerStateChanged(newState: Int) {
                if(homeActionMode != null){
                    homeActionMode!!.finish()
                }
                if(recycleActionMode != null){
                    recycleActionMode!!.finish()
                }
            }
        })
        observeUiPreferences()
        viewModel.observeLanguagePreference(this)
        observeBiometricPreferences()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun observeUiPreferences() {
        Log.d("Home", "observeUiPreferences called")
//        viewModel.observeLanguagePreference(this)

        uiSharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val darkMode = uiSharedPreferences.getBoolean("darkMode", false)
        if(darkMode){
            onDarkMode()
        }
        else{
            onLightMode()
        }
    }
    private fun onLightMode() {
        isDarkMode = false
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        darkSwitch.isChecked = false
        Log.d("Home", "onLightMode called")
    }

    private fun onDarkMode() {
        isDarkMode =true
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        darkSwitch.isChecked = true
        Log.d("Home", "onDarkMode called")

    }


    /*------------------------------------------------------------------------------------------------------------------------------------
    Biometric Login Feature
     */
    private fun observeBiometricPreferences(){
        Log.d("Home", "observeBiometricPreferences called")

        biometricSharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val biometric = biometricSharedPreferences.getBoolean("biometric", false)
        if(biometric){
            onBiometricEnabled()
        }
        else{
            onBiometricDisabled()
        }
    }
    private fun onBiometricEnabled(){
        Log.d("Home", "onBiometricEnabled called")

        isBiometricEnable = true
        val biometricManager = this.let { BiometricManager.from(this) }
        when (biometricManager.canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                        )
                    }
                } else {
                    TODO("VERSION.SDK_INT < R")
                }
                ActivityCompat.startActivityForResult(
                    this,
                    enrollIntent,
                    requestCode,
                    null
                )
            }
        }

        executor = this.let { ContextCompat.getMainExecutor(this) }
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
                    Log.e("Auth Succeed", "Authentication Succeed")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.e("Auth Failed", "Authentication failed")
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.authenticate_with_biometric))
            .setAllowedAuthenticators(
                BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()

        screenLock.isChecked = true
        biometricPrompt.authenticate(promptInfo)
    }
    private fun onBiometricDisabled(){
        isBiometricEnable = false
        screenLock.isChecked = false
        Log.d("Home", "onBiometricDisabled called")

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key.equals("darkMode"))  {
            observeUiPreferences()
//            Log.d("Home", "observeUiPreferences called")
//            recreate()

//            finish()
//            startActivity(intent)
//            overridePendingTransition(0, 0)
        }
        if(key.equals("biometric"))  {
            observeBiometricPreferences()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        uiSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        biometricSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}

const val ADD_TASK_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val PROFILE_UPDATED_RESULT_OK = Activity.RESULT_FIRST_USER + 3
const val CHANNEL_ID: String = "4"
const val CHANNEL_NAME: String = "Notefy"
const val CHANNEL_DESCRIPTION = "Reminder Message"


