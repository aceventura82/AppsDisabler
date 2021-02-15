package com.servoz.appsdisabler.config

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.servoz.appsdisabler.LauncherActivity
import com.servoz.appsdisabler.R
import kotlinx.android.synthetic.main.fragment_apps.*
import kotlinx.android.synthetic.main.help_apps.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*


class AppsListFragment: Fragment(),androidx.appcompat.widget.SearchView.OnQueryTextListener {

    private lateinit var searchAdapter: AppsListRecyclerAdapter
    private var prefFile: String = "com.servoz.appsdisabler.prefs"
    private var prefs: SharedPreferences? = null

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
        prefs = requireContext().getSharedPreferences(prefFile, 0)
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
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        if(requireActivity().intent.getStringExtra("CONFIG")!="1" && prefs!!.getString("HELP_APPS","")=="")
            doAsync {
                Thread.sleep(2000)
                uiThread { showHelp() }
            }
    }


    private fun listApps():MutableList<SearchApps>{
        val prefFile = "com.servoz.appsdisabler.prefs"
        val prefs = requireContext().getSharedPreferences(prefFile, 0)
        val systemApps=prefs!!.getString("SYSTEM","")=="ON"
        val pm = requireContext().packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val apps= mutableListOf<SearchApps>()
        val mask = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
        for (packageInfo in packages) {
            if(packageInfo.packageName == "com.servoz.appsdisabler")
                continue
            if(systemApps || (!systemApps && packageInfo.flags and mask==0)){
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

    private fun showHelp(){
        val helpWindow= PopupWindow(requireContext())
        val windowView=LayoutInflater.from(requireContext()).inflate(R.layout.help_apps, CardTitleMyGames, false)
        helpWindow.contentView=windowView
        helpWindow.isOutsideTouchable=true
        windowView.layoutHelp.setBackgroundColor(requireContext().getColor(R.color.colorBG))
        windowView.layoutHelp.background.alpha=255
        windowView.checkBoxHelpApp.isChecked=prefs!!.getString("HELP_APPS","")=="OFF"
        helpWindow.showAtLocation(textItemTitle2, Gravity.CENTER, 0, 0)
        windowView.checkBoxHelpApp.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked)
                prefs!!.edit().putString("HELP_APPS", "OFF").apply()
            else
                prefs!!.edit().putString("HELP_APPS", "").apply()
        }
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