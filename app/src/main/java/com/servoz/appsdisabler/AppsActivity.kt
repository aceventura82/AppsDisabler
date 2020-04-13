package com.servoz.appsdisabler

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.navigation.fragment.NavHostFragment
import com.servoz.appsdisabler.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess


class AppsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(intent.getStringExtra("CONFIG")=="1"){
            NavHostFragment.findNavController(nav_host_fragment).navigate(R.id.action_global_config, Bundle())
        }
    }
}
