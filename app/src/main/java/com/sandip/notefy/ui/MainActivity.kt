package com.sandip.notefy.ui

import android.app.Activity
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        observeUiPreferences(applicationContext)
        observeLanguagePreference(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        Log.d("TAG","onCreate main")
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
                    val imageUri = Uri.parse(it.image)
                    this.let { it1 -> Glide.with(it1).load(imageUri).into(userPhoto) }
                }
            }}

        darkSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onDarkModeToggle(isChecked)
        }

        screenLock.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onScreenLockToggle(isChecked)
        }

        viewModel.observeBiometricPreferences(this)
        viewModel.updateUiSwitch()

        this.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                when (event) {
                    is HomeViewModel.TasksEvent.StartIntent -> {
                        ActivityCompat.startActivityForResult(
                            event.mainActivity,
                            event.enrollIntent,
                            requestCode,
                            null
                        )
                    }
                    is HomeViewModel.TasksEvent.AuthenticatePrompt -> {
                        event.biometricPrompt.authenticate(event.promptInfo)
                    }
                    is HomeViewModel.TasksEvent.SetValue -> {
                        isBiometricEnable = true
                        screenLock.isChecked = true
                    }
                    is HomeViewModel.TasksEvent.BiometricDisabled -> {
                        isBiometricEnable = false
                        screenLock.isChecked = false
                        viewModel.onScreenRotate(true)
                    }
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
            viewModel.setUiPreferences()
        }
        if(key.equals("biometric"))  {
            viewModel.observeBiometricPreferences(this)
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



