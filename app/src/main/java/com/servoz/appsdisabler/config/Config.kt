package com.servoz.appsdisabler.config

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.fragment.app.Fragment
import com.servoz.appsdisabler.LauncherActivity
import com.servoz.appsdisabler.R
import kotlinx.android.synthetic.main.fragment_config.*
import petrov.kristiyan.colorpicker.ColorPicker
import petrov.kristiyan.colorpicker.ColorPicker.OnChooseColorListener


class Config:Fragment() {

    private var prefFile: String = "com.servoz.appsdisabler.prefs"
    private var prefs: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val callback = object : OnBackPressedCallback(true ) {
            override fun handleOnBackPressed() {
                val intent = Intent(requireContext(), LauncherActivity::class.java)
                requireActivity().finish()
                startActivity(intent)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        return inflater.inflate(R.layout.fragment_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        prefs = requireContext().getSharedPreferences(prefFile, 0)
        theme()
        columns()
        launcherHeight()
        changeSwitch("SYSTEM",switchSysApps)
        changeSwitch("LABELS",switchHideLabels)
        changeSwitch("SHOW_TABS",switchTabs)

        setColor("TEXT", requireContext().getColor(R.color.design_default_color_on_primary), buttonTextColor)
        setColor("TEXT2", requireContext().getColor(R.color.colorAppDisabled), buttonDisTextColor)
        setColor("ICONS_THEME", requireContext().getColor(R.color.colorIcons), buttonIconsColor)
        setColor("ITEM_SEL", requireContext().getColor(R.color.colorAppSelected), buttonItemSelectedColor)

        backgroundColors()
        resetHelps()
    }

    private fun theme(){
        when(prefs!!.getString("THEME","System")){
            "Light" -> {radioButtonThemeLight.isChecked=true}
            "Dark" -> {radioButtonThemeDark.isChecked=true}
            else -> {radioButtonTHemeSys.isChecked=true}
        }

        radioButtonTHemeSys.setOnClickListener {
            prefs!!.edit().putString("THEME", "System").apply()
            setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        }
        radioButtonThemeDark.setOnClickListener {
            prefs!!.edit().putString("THEME", "Dark").apply()
            setDefaultNightMode(MODE_NIGHT_YES)
        }
        radioButtonThemeLight.setOnClickListener {
            prefs!!.edit().putString("THEME", "Light").apply()
            setDefaultNightMode(MODE_NIGHT_NO)
        }
    }

    private fun columns(){
        var col=prefs!!.getInt("my_apps_columns",6)
        columnsCount.progress=col
        textViewCols.text=col.toString()

        columnsCount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textViewCols.text =progress.toString()
                col=progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                prefs!!.edit().putInt("my_apps_columns", col).apply()
            }
        })
    }

    private fun changeSwitch(key:String, view:Switch){
        view.isChecked=prefs!!.getString(key,"")!=""

        view.setOnClickListener{
            if(view.isChecked)
                prefs!!.edit().putString(key, "ON").apply()
            else
                prefs!!.edit().putString(key, "").apply()
        }
    }

    private fun launcherHeight(){
        var value=prefs!!.getInt("HEIGHT",5)
        launcherHeight.progress= value
        textViewHeight.text=value.toString()

        launcherHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                value= progress
                textViewHeight.text =value.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                prefs!!.edit().putInt("HEIGHT", value).apply()
            }
        })
    }

    private fun backgroundColors(){
        setColor("BG", requireContext().getColor(R.color.design_default_color_primary), buttonBgColor)
        var cAlpha=prefs!!.getInt("ALPHA",50)
        bgAlpha.progress=cAlpha
        textAlpha.text=cAlpha.toString()

        bgAlpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textAlpha.text =progress.toString()
                cAlpha=progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                prefs!!.edit().putInt("ALPHA", cAlpha).apply()
            }
        })
    }
    
    private fun setColor(key:String, defaultColor:Int, view:View){
        val colors= arrayListOf(
            "#d50000","#F44336","#E91E63","#FF5722","#FF9800","#9C27B0","#673AB7","#3F51B5","#2196F3","#03A9F4",
            "#00BCD4","#009688","#4CAF50","#8BC34A","#CDDC39","#FFEB3B","#FFC107",
            "#795548","#3E2723","#9E9E9E","#607D8B","#607D8B","#BDBDBD","#FFFFFF", "#000000")
         if(prefs!!.getString(key,"")!="")
             view.setBackgroundColor(Color.parseColor(prefs!!.getString(key,"")))
        else
             view.setBackgroundColor(defaultColor)

        view.setOnClickListener {
            val colorPicker = ColorPicker(activity)
            colorPicker.setColors(colors)
            colorPicker.show()
            colorPicker.setOnChooseColorListener(object : OnChooseColorListener {
                override fun onChooseColor(position: Int, color: Int) {
                    prefs!!.edit().putString(key, colors[position]).apply()
                    view.setBackgroundColor(Color.parseColor(colors[position]))
                }
                override fun onCancel() {}
            })
        }

    }

    private fun resetHelps(){
        reset_helps_button.setOnClickListener {
            prefs!!.edit().putString("HELP_APPS", "").apply()
            prefs!!.edit().putString("HELP_LAUNCHER", "").apply()
            Toast.makeText(requireContext(), getString(R.string.help_reset), Toast.LENGTH_SHORT).show()
        }
    }

}