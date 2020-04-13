package com.servoz.appsdisabler.config

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.servoz.appsdisabler.AppsActivity
import com.servoz.appsdisabler.LauncherActivity
import com.servoz.appsdisabler.R
import kotlinx.android.synthetic.main.fragment_apps.*
import java.util.*


class AppsListFragment: Fragment(),androidx.appcompat.widget.SearchView.OnQueryTextListener {

    private lateinit var searchAdapter: AppsListRecyclerAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_apps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // println("My Games DEBUG:Home")
        //adding a layout manager
        recyclerViewApps.layoutManager = GridLayoutManager(context,1)
        val objSearch = listApps()
        searchAdapter =
            AppsListRecyclerAdapter(objSearch)
        recyclerViewApps.adapter = searchAdapter
        searchV.setOnQueryTextListener(this)
        loadingApps.isVisible =false

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    val intent = Intent(requireContext(), LauncherActivity::class.java)
                    requireActivity().finish()
                    startActivity(intent)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

    }

    private fun listApps():MutableList<SearchApps>{
        val prefFile = "com.servoz.appsdisabler.prefs"
        val prefs = requireContext().getSharedPreferences(prefFile, 0)
        val systemApps=prefs!!.getString("SYSTEM","")=="ON"
        val pm = requireContext().packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val apps= mutableListOf<SearchApps>()
        for (packageInfo in packages) {
            if(packageInfo.packageName == "com.servoz.appsdisabler")
                continue
            if(systemApps || (!systemApps && packageInfo.sourceDir.contains( "/data/app/"))){
                val status = requireActivity().packageManager.getApplicationInfo(packageInfo.packageName, 0).enabled
                apps.add(
                    SearchApps(
                        arrayListOf(
                            packageInfo.packageName,
                            pm.getApplicationLabel(packageInfo).toString(),
                            if (status) "1" else "0"
                        )
                    )
                )
            }
        }
        apps.sortBy{it.data[1].toLowerCase(Locale.ROOT)}
        return apps
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        search(query)
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        search(newText)
        return true
    }

    private fun search(s: String?) {
        searchAdapter.search(s) {
            Toast.makeText(context, "Not Found", Toast.LENGTH_SHORT).show()
        }
    }
}