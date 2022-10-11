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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.sandip.notefy.R
import com.sandip.notefy.databinding.ActivityMainBinding
import com.sandip.notefy.ui.home.NoteAdapter.Companion.homeActionMode
import com.sandip.notefy.ui.recycle_bin.RecycleAdapter.Companion.recycleActionMode
import com.sandip.notefy.util.PreferencesManager
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener{
    private val viewModel: MainActivityViewModel by viewModels()
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
        preferencesManager = PreferencesManager(applicationContext)

        installSplashScreen()
        super.onCreate(savedInstanceState)
        Log.d("Sandip", "Inside Main")

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Language

        val navigationView = binding.navView
        darkSwitch =
            navigationView.menu.findItem(R.id.dark_theme).actionView!!.findViewById(R.id.dark_switch)

        //Dark Mode
        darkSwitch.setOnCheckedChangeListener { _, isChecked ->
//            when (isChecked) {
//                true -> viewModel.onDarkTheme()
//
//                false -> viewModel.onLightTheme()
//            }
            val sharedPreferences =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
            var editor = sharedPreferences.edit()
            editor.putBoolean("darkMode",isChecked)
            editor.commit()
            Log.d("Sandip", "inside switch")


        }

//
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


        screenLock =
            navigationView.menu.findItem(R.id.screen_lock).actionView!!.findViewById(R.id.biometric_switch)


        screenLock.setOnCheckedChangeListener { _, isChecked ->
//            when (isChecked) {
//                true -> viewModel.onScreenLockEnabled()
////                    Toast.makeText(this, "Screen Lock Enabled", Toast.LENGTH_LONG).show()
//                false -> viewModel.onScreenLockDisabled()
//            }
            val sharedPreferences =  getSharedPreferences("BIOMETRIC",Context.MODE_PRIVATE)
            var editor = sharedPreferences.edit()
            editor.putBoolean("biometric",isChecked)
            editor.commit()


        }

        lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when (event) {
                    is MainActivityViewModel.MainTaskEvent.UpdateDarkUI -> {
                        Snackbar.make(view, "Dark Mode Enabled", Snackbar.LENGTH_SHORT).show()
                    }
                    is MainActivityViewModel.MainTaskEvent.UpdateLightUI -> {
                        Snackbar.make(view, "Dark Mode Disabled", Snackbar.LENGTH_SHORT).show()
                    }
                    else -> {}
                }.exhaustive
            }
        }
        observeUiPreferences()

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

        observeBiometricPreferences()

    }




    override fun onDestroy() {
        super.onDestroy()
//        val sharedPreferences =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
//        var editor = sharedPreferences.edit()
//        editor.remove("darkMode");
//        editor.commit();
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    private fun observeUiPreferences() {
//        preferencesManager.uiModeFlow.asLiveData().observe(this) { uiMode ->
//            when (uiMode) {
//                UiMode.LIGHT -> onLightMode()
//                UiMode.DARK -> onDarkMode()
//            }
//        }
//        }
        val sharedPreferences =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val darkMode = sharedPreferences.getBoolean("darkMode", false)
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


    }

    private fun onDarkMode() {
        isDarkMode =true
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        darkSwitch.isChecked = true
    }


/*------------------------------------------------------------------------------------------------------------------------------------
Biometric Login Feature
 */

    private fun observeBiometricPreferences(){
        val sharedPreferences =  getSharedPreferences("BIOMETRIC",Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val biometric = sharedPreferences.getBoolean("biometric", false)
        if(biometric){
            onBiometricEnabled()
        }
        else{
            onBiometricDisabled()
        }
    }
    private fun onBiometricEnabled(){
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

        executor = this.let { ContextCompat.getMainExecutor(this) }!!
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
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.e("Auth Failed", "Authentication failed")

//                    finish()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate with Biometric")
            .setAllowedAuthenticators(
                BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()

        screenLock.isChecked = true
        biometricPrompt.authenticate(promptInfo)
    }
    private fun onBiometricDisabled(){
        isBiometricEnable = false
        screenLock.isChecked = false
    }




    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key.equals("darkMode"))  {
            observeUiPreferences()
//            finish();
//            startActivity(intent);
//            overridePendingTransition(0, 0);
            Log.d("Sandip", "inside methods")
        }
        if(key.equals("biometric"))  {
            observeBiometricPreferences()
//            finish();
//            startActivity(intent);
//            overridePendingTransition(0, 0);

        }
//        if(key.equals("position"))  {
//            observeUiPreferences()
//            finish();
//            startActivity(intent);
//            overridePendingTransition(0, 0);
//            Log.d("Sandip", "inside methods")
//        }
    }



}
const val ADD_TASK_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val PROFILE_UPDATED_RESULT_OK = Activity.RESULT_FIRST_USER + 3


