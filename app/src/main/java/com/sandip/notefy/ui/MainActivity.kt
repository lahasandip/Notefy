package com.sandip.notefy.ui

import android.app.Activity
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import butterknife.ButterKnife
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.sandip.notefy.R
import com.sandip.notefy.databinding.ActivityMainBinding
import com.sandip.notefy.ui.help.HelpFeedback
import com.sandip.notefy.ui.home.HomeDirections
import com.sandip.notefy.ui.home.HomeViewModel
import com.sandip.notefy.ui.newupdate.NewUpdateNoteViewModel
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.newCoroutineContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration


    private lateinit var navController: NavController
    companion object{
        var drawerLayout: DrawerLayout? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
//
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        drawerLayout = binding.drawerLayout
        val navigationView = binding.navView
        val headerView = navigationView.getHeaderView(0)
        val profileSection = headerView.findViewById<LinearLayout>(R.id.profile_section)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fragment_home, R.id.fragment_newUpdateNote, R.id.fragment_helpfeedback,
                R.id.fragment_about, R.id.fragment_settings, R.id.fragment_languages
            ), drawerLayout
        )
        navigationView.setupWithNavController(navController)

        val darkSwitch = navigationView.menu.findItem(R.id.dark_theme).actionView.findViewById<SwitchMaterial>(R.id.dark_switch);
        val screenLock = navigationView.menu.findItem(R.id.screen_lock).actionView.findViewById<SwitchMaterial>(R.id.dark_switch);
        darkSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) viewModel.onDarkTheme()
            if(!(isChecked)) viewModel.onLightTheme()
        }


        screenLock.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                Toast.makeText(this, "Screen Lock Enabled", Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(this, "Screen Lock Disabled", Toast.LENGTH_LONG).show()
            }
        }

        profileSection.setOnClickListener {

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
                }.exhaustive
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    }
}
const val ADD_TASK_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 2
