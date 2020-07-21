package com.servoz.appsdisabler

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.activity_main.*


class AppsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(intent.getStringExtra("CONFIG")=="1"){
            NavHostFragment.findNavController(nav_host_fragment).navigate(R.id.action_global_config, Bundle())
        }
    }
}
