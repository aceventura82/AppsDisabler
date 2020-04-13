package com.servoz.appsdisabler.config

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
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
        sysApps()
        labels()
        launcherHeight()

        val colors= arrayListOf("#FFFFFF", "#000000", "#aa0000", "#aa5500", "#005500",
            "#00007f", "#aa007f", "#00557f", "#00aa7f", "#0000ff",
            "#ff5500", "#550000", "#55007f", "#555500", "#55557f",
            "#55aaff", "#5f5f5f", "#ffff00", "#ff00ff", "#d10000")

        backgroundColors(colors)
        enabledColor(colors)
        disabledColor(colors)
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

    private fun sysApps(){
        switchSysApps.isChecked=prefs!!.getString("SYSTEM","")=="ON"

        switchSysApps.setOnClickListener{
            if(switchSysApps.isChecked)
                prefs!!.edit().putString("SYSTEM", "ON").apply()
            else {
                prefs!!.edit().putString("SYSTEM", "").apply()
            }
        }
    }

    private fun labels(){
        switchHideLabels.isChecked=prefs!!.getString("LABELS","")!="OFF"

        switchHideLabels.setOnClickListener{
            if(switchHideLabels.isChecked)
                prefs!!.edit().putString("LABELS", "").apply()
            else {
                prefs!!.edit().putString("LABELS", "OFF").apply()
            }
        }
    }

    private fun launcherHeight(){
        var value=prefs!!.getString("HEIGHT","HALF")
        launcherHeight.progress= when(value){
            "FULL" -> 5
            "HIGH" -> 4
            "MIN" -> 2
            "BOTTOM" -> 1
            else -> 3
        }
        textViewHeight.text=value

        launcherHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                value= when(progress){
                    5 ->"FULL"
                    4 -> "HIGH"
                    2 -> "MIN"
                    1->"BOTTOM"
                    else -> "HALF"
                }
                textViewHeight.text =value
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                prefs!!.edit().putString("HEIGHT", value).apply()
            }
        })
    }

    private fun backgroundColors(colors:ArrayList<String>){
        if(prefs!!.getString("BG","")!="")
            buttonBgColor.setBackgroundColor(Color.parseColor(prefs!!.getString("BG","")))
        else
            buttonBgColor.setBackgroundColor(requireContext().getColor(R.color.design_default_color_primary))

        buttonBgColor.setOnClickListener {
            val colorPicker = ColorPicker(activity)
            colorPicker.setColors(colors)
            colorPicker.show()
            colorPicker.setOnChooseColorListener(object : OnChooseColorListener {
                override fun onChooseColor(position: Int, color: Int) {
                    prefs!!.edit().putString("BG", colors[position]).apply()
                    buttonBgColor.setBackgroundColor(Color.parseColor(colors[position]))
                }
                override fun onCancel() {}
            })
        }

        var cAlpha=prefs!!.getInt("ALPHA",100)
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

    private fun enabledColor(colors:ArrayList<String>){
        if(prefs!!.getString("TEXT","")!="")
            buttonTextColor.setBackgroundColor(Color.parseColor(prefs!!.getString("TEXT","")))
        else
            buttonTextColor.setBackgroundColor(requireContext().getColor(R.color.design_default_color_on_primary))

        buttonTextColor.setOnClickListener {
            val colorPicker = ColorPicker(activity)
            colorPicker.setColors(colors)
            colorPicker.show()
            colorPicker.setOnChooseColorListener(object : OnChooseColorListener {
                override fun onChooseColor(position: Int, color: Int) {
                    prefs!!.edit().putString("TEXT", colors[position]).apply()
                    buttonTextColor.setBackgroundColor(Color.parseColor(colors[position]))
                }
                override fun onCancel() {}
            })
        }

    }

    private fun disabledColor(colors:ArrayList<String>){
         if(prefs!!.getString("TEXT2","")!="")
            buttonDisTextColor.setBackgroundColor(Color.parseColor(prefs!!.getString("TEXT2","")))
        else
            buttonDisTextColor.setBackgroundColor(Color.RED)

        buttonDisTextColor.setOnClickListener {
            val colorPicker = ColorPicker(activity)
            colorPicker.setColors(colors)
            colorPicker.show()
            colorPicker.setOnChooseColorListener(object : OnChooseColorListener {
                override fun onChooseColor(position: Int, color: Int) {
                    prefs!!.edit().putString("TEXT2", colors[position]).apply()
                    buttonDisTextColor.setBackgroundColor(Color.parseColor(colors[position]))
                }
                override fun onCancel() {}
            })
        }

    }

}