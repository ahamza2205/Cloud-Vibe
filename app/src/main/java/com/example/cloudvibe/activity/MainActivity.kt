package com.example.cloudvibe.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.cloudvibe.R
import com.example.cloudvibe.alert.view.WeatherAlertFragment
import com.example.cloudvibe.favorit.view.FavoritFragment
import com.example.cloudvibe.home.view.HomeFragment
import com.example.cloudvibe.setting.view.SettingFragment
import com.example.cloudvibe.sharedpreferences.SharedPreferencesHelper
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.tool_bar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle =
            ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment(), getString(R.string.home)) // Set title for HomeFragment
            navigationView.setCheckedItem(R.id.nav_home)
        }

        checkAndChangLocality()
    }

    fun replaceFragment(fragment: Fragment, title: String = "") {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

        // Set the toolbar title
        supportActionBar?.title = title
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> replaceFragment(HomeFragment(), getString(R.string.home))
            R.id.nav_setting -> replaceFragment(SettingFragment(), getString(R.string.settings))
            R.id.nav_alarts -> replaceFragment(WeatherAlertFragment(), getString(R.string.alerts))
            R.id.nav_favorites -> replaceFragment(FavoritFragment(), getString(R.string.favorites))
            R.id.nav_exit -> finish()
        }
        item.isChecked = true
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val fragment = HomeFragment()
                    val bundle = Bundle().apply {
                        putString("city_name", it)
                    }
                    fragment.arguments = bundle

                    replaceFragment(fragment, getString(R.string.search_results))
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
     fun checkAndChangLocality()
    {
        val languageCode = SharedPreferencesHelper(this).getLanguage()
        val locale = resources.configuration.locales[0]

        if(locale.language != languageCode)
        {

            val newLocale = Locale(languageCode ?: "en")
            Locale.setDefault(newLocale)

            val config = resources.configuration

            config.setLocale(newLocale)
            config.setLayoutDirection(newLocale)

            resources.updateConfiguration(config,resources.displayMetrics)

            recreate()

           }
        }

}
