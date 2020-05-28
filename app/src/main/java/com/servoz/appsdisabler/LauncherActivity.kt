package com.servoz.appsdisabler

import android.app.ActivityManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.servoz.appsdisabler.tools.Db
import com.servoz.appsdisabler.tools.RunCommand
import kotlinx.android.synthetic.main.help_launcher.view.*
import kotlinx.android.synthetic.main.launcher_layout.*
import kotlinx.android.synthetic.main.slide_fragment.*
import kotlinx.android.synthetic.main.tag_name.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.collections.ArrayList


val Int.dp: Int
   get() = (this * Resources.getSystem().displayMetrics.density).toInt()

class LauncherActivity : AppCompatActivity() {

    private var prefFile: String = "com.servoz.appsdisabler.prefs"
    private var prefs: SharedPreferences? = null
    private var currentTag=""
    private var tabsPages= arrayListOf<TagView>()
    private var textColor=0
    private var textColor2=0
    private var iconsColors=0
    private var itemColor=0
    private var baseColor=0
    private var alpha=50
    private lateinit var mPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_layout)

        prefs = getSharedPreferences(prefFile, 0)
        val objCmd= RunCommand()
        if(prefs!!.getString("LABELS","-1")=="-1")
            prefs!!.edit().putString("LABELS", "ON").apply()

        if(prefs!!.getString("RECENT_APPS","")=="ON")
            hideRecent()
        setTheme()
        setLauncherSize()
        setColors()
        mainMyAppsLayout.setOnClickListener { finish() }
        buttonLauncherMenu.setOnClickListener{showConfigMenu( objCmd)}
        val tagsPagesNames=ArrayList<String>()
        for (i in 0 until 10)
            tagsPagesNames.add(prefs!!.getString("TAG_$i","")!!)
        tabsPages.add(TagView("", prefs!!.getInt("my_apps_columns",6), textColor, textColor2,"",
            prefs!!.getString("LABELS","")!!, tagsPagesNames))
        if(prefs!!.getString("SHOW_TABS","")=="ON"){
            createTagsViews(tagsPagesNames)
            newTab()
        }else{
            linearLayoutTags.isVisible=false
            currentTag=""
            prefs!!.edit().putString("CURRENT_TAG", "").apply()
        }

        mPager = findViewById(R.id.pager)
        val pagerAdapter = PagerAdapter(supportFragmentManager, tabsPages)
        mPager.adapter = pagerAdapter
        tabLayoutLauncher.setupWithViewPager(mPager)
        addTabsListener(objCmd)
        setInitialTab()
        if(prefs!!.getString("HELP_LAUNCHER","")=="")
            doAsync {
                Thread.sleep(2000)
                uiThread { showHelp() }
            }
    }

    private fun hideRecent(){
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val tasks = am.appTasks
        if (tasks != null && tasks.size > 0) {
            tasks[0].setExcludeFromRecents(true)
        }
    }

    private fun setTheme(){
        when(prefs!!.getString("THEME","")){
            "Dark" -> {
                setDefaultNightMode(MODE_NIGHT_YES)
            }
            "Light" -> {
                setDefaultNightMode(MODE_NIGHT_NO)
            }
        }
    }

    private fun setColors(){
        baseColor = if(prefs!!.getString("BG","")=="")
            Color.BLACK
        else
            Color.parseColor(prefs!!.getString("BG",""))
        textColor = if(prefs!!.getString("TEXT","")=="")
            getColor(R.color.design_default_color_on_primary)
        else
            Color.parseColor(prefs!!.getString("TEXT",""))
        textColor2 = if(prefs!!.getString("TEXT2","")=="")
            getColor(R.color.colorAppDisabled)
        else
            Color.parseColor(prefs!!.getString("TEXT2",""))
        iconsColors = if(prefs!!.getString("ICONS_THEME","")=="")
            getColor(R.color.colorIcons)
        else
            Color.parseColor(prefs!!.getString("ICONS_THEME",""))
        itemColor = if(prefs!!.getString("ITEM_SEL","")=="")
            getColor(R.color.colorAppSelected)
        else
            Color.parseColor(prefs!!.getString("ITEM_SEL",""))

        alpha = (prefs!!.getInt("ALPHA",50) / 100f * 255).toInt()
        linearLayoutMain.setBackgroundColor(baseColor)
        linearLayoutMain.background.alpha=alpha

        textViewAddTab.setColorFilter(iconsColors)
        buttonLauncherMenu.setColorFilter(iconsColors)
        buttonLauncherConfig.setColorFilter(iconsColors)
        buttonLauncherEditApps.setColorFilter(iconsColors)
        buttonLauncherDisableAll.setColorFilter(iconsColors)
        buttonLauncherEnableAll.setColorFilter(iconsColors)
    }

    private fun createTagsViews(tagsPagesNames:ArrayList<String>){
        val cols=prefs!!.getInt("my_apps_columns",6)
        val labels=prefs!!.getString("LABELS","")!!
        for (i in 0 until 10){
            val tt=prefs!!.getString("TAG_$i","")!!
            if(tt!="") {
                tabsPages.add(
                    TagView(
                        "TAG_$i", cols, textColor, textColor2,
                        prefs!!.getString("TAG_$i", "")!!, labels, tagsPagesNames
                    )
                )
            }
        }
    }

    private fun addTabsListener(objCmd:RunCommand){
        val tabStrip: LinearLayout = tabLayoutLauncher.getChildAt(0) as LinearLayout
        for (i in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(i).setOnLongClickListener {
                showTagTitleMenu(tabStrip.getChildAt(i),objCmd,tabsPages[i].tag)
                true
            }
        }
        if(prefs!!.getString("SHOW_ALL_TABS_B","")=="ON"){
            tabStrip.getChildAt(0).setBackgroundResource(R.drawable.ic_view_all)
            tabStrip.getChildAt(0).layoutParams=LinearLayout.LayoutParams(80,80)
            tabStrip.getChildAt(0).isVisible=true
        }else
            tabStrip.getChildAt(0).isVisible=false
        tabLayoutLauncher.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                prefs!!.edit().putString("CURRENT_TAG", tabsPages[tabLayoutLauncher.selectedTabPosition].tag).apply()
                currentTag = tabsPages[tabLayoutLauncher.selectedTabPosition].tag
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun newTab(){
        textViewAddTab.setOnClickListener {
            var v=true
            for (i in 0 until 10){
                if(prefs!!.getString("TAG_$i","")==""){
                    showTagName(textViewAddTab,i)
                    v=false
                    break
                }
            }
            if(v){
                Toast.makeText(this, getString(R.string.NoMoreTabs), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setInitialTab(){
        currentTag=prefs!!.getString("CURRENT_TAG","")!!
        if(currentTag!=""){
            for ((c,tag) in tabsPages.withIndex()){
                if(tag.tag==currentTag){
                    tabLayoutLauncher.getTabAt(c)!!.select()
                    break
                }
            }
        }
    }

    private fun showTagName(view: View, i:Int, rename:Boolean=false){
        val passWindow=PopupWindow(this)
        val windowView=LayoutInflater.from(this).inflate(R.layout.tag_name, gridMyApps, false)
        passWindow.contentView=windowView
        passWindow.isFocusable=true
        passWindow.width=(resources.displayMetrics.widthPixels-10.dp)/2
        passWindow.showAsDropDown(view)
        windowView.create_tag_cancel.setOnClickListener{
            passWindow.dismiss()
        }
        windowView.create_tag_save.setOnClickListener {
            val tagName=windowView.editText_tag_name.text.toString().trim()
            var createOk=true
            if(tagName!=""){
                for (pos in 0 until 10){
                    //check unique name, if renaming check no same position
                    if(prefs!!.getString("TAG_$pos","")==tagName && (!rename || pos!=i)){
                        createOk=false
                    }
                }
                if(createOk){
                    prefs!!.edit().putString("TAG_$i", tagName).apply()
                    passWindow.dismiss()
                    val intent = Intent(this, LauncherActivity::class.java)
                    finish()
                    startActivity(intent)
                }
                else
                    Toast.makeText(this, getString(R.string.DuplicatedName), Toast.LENGTH_SHORT).show()
            }else
                Toast.makeText(this, getString(R.string.WrongName), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setLauncherSize(){
        val cellSize = (resources.displayMetrics.heightPixels-25.dp)/10
        val h= prefs!!.getInt("HEIGHT",5)*cellSize
        linearLayoutMain.layoutParams= LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            if(h>resources.displayMetrics.heightPixels)resources.displayMetrics.heightPixels else h,
            0f)
    }

    private fun changeAll(objCmd:RunCommand, enable:Boolean=false,tag:String=""){
        val dbHandler = Db(this, null)
        val apps=dbHandler.getData("app", if(tag!="")"`tag` like '%|$tag|%'" else "")
        apps.sortBy{it[1].toLowerCase(Locale.ROOT)}
        for((c,app) in apps.withIndex()){
            if(app[2] == "1")
                continue
            objCmd.sudoForResult("pm ${if (enable) "enable" else "disable" } ${app[0]}")
            try {
                findViewById<TextView>(c).setTextColor(if (enable) textColor else textColor2)
            }catch (ex:NullPointerException){}
        }
    }

    private fun showConfigMenu(objCmd: RunCommand) {
        animateMenu()
        buttonLauncherConfig.setOnClickListener {
            finish()
            val intent = Intent(this, AppsActivity::class.java)
            intent.putExtra("CONFIG", "1")
            startActivity(intent)
        }
        buttonLauncherEditApps.setOnClickListener {
            finish()
            val intent = Intent(this, AppsActivity::class.java)
            intent.putExtra("TAG", currentTag)
            startActivity(intent)
        }
        buttonLauncherDisableAll.setOnClickListener {
            doAsync {
                changeAll(objCmd)
                uiThread {
                    Toast.makeText(this@LauncherActivity, getString(R.string.AllAppsDisabled), Toast.LENGTH_SHORT).show()
                    finish()
                    val intent = Intent(this@LauncherActivity, LauncherActivity::class.java)
                    startActivity(intent)
                }
            }
            animateMenu()
        }
        buttonLauncherEnableAll.setOnClickListener {
            doAsync {
                changeAll(objCmd, true)
                uiThread {
                    Toast.makeText(this@LauncherActivity, getString(R.string.AllAppsEnabled), Toast.LENGTH_SHORT).show()
                    finish()
                    val intent = Intent(this@LauncherActivity, LauncherActivity::class.java)
                    startActivity(intent)
                }
            }
        }

    }

    private fun animateMenu(){
        val p1:Float
        val p2:Float
        if(!configLayout.isVisible){
            p1=configLayout.width.toFloat()
            p2=0f
            configLayout.isVisible=true
            buttonLauncherMenu.setImageDrawable(getDrawable(R.drawable.ic_arrow_right))
        }
        else{
            p1=0f
            p2=configLayout.width.toFloat()
            configLayout.isVisible=false
            buttonLauncherMenu.setImageDrawable(getDrawable(R.drawable.ic_arrow_left))
        }
        val animate = TranslateAnimation(p1, p2, 0f, 0f)
        animate.duration = 500
        //animate.fillAfter = true
        configLayout.startAnimation(animate)
    }

    private fun showTagTitleMenu(view: View, objCmd: RunCommand,tagId:String) {
        val popupConfig = PopupMenu(this, view)
        popupConfig.inflate(R.menu.menu_tag_title)
        popupConfig.setOnMenuItemClickListener{ item: MenuItem? ->
            when (item!!.itemId) {
                R.id.menuTagRename -> {
                    showTagName(textViewAddTab,Integer.parseInt(tagId.replace("TAG_","")),true)
                }
                R.id.menuTagDelete -> {
                    val dbHandler = Db(this, null)
                    doAsync {
                        prefs!!.edit().remove(tagId).apply()
                        dbHandler.replaceFieldData("`app`","`tag`", "|$tagId|", "" )
                    }
                    prefs!!.edit().remove(tagId).apply()
                    prefs!!.edit().putString("CURRENT_TAG", "").apply()
                    Toast.makeText(this, tagId, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LauncherActivity::class.java)
                    finish()
                    startActivity(intent)
                }
                R.id.menuTagDisable -> {
                    changeAll(objCmd, tag=tagId)
                }
            }
            true
        }
        popupConfig.show()
    }

    private fun showHelp(){
        val helpWindow= PopupWindow(this)
        val windowView=LayoutInflater.from(this).inflate(R.layout.help_launcher, mainMyAppsLayout, false)
        helpWindow.contentView=windowView
        helpWindow.isOutsideTouchable=true
        windowView.layoutHelp.setBackgroundColor(getColor(R.color.colorBG))
        windowView.layoutHelp.background.alpha=255
        windowView.checkBoxHelpApp.isChecked=prefs!!.getString("HELP_LAUNCHER","")=="OFF"
        linearLayoutMain.post{
            helpWindow.showAtLocation(linearLayoutMain, Gravity.CENTER, 0, 0)
        }
        windowView.checkBoxHelpApp.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked)
                prefs!!.edit().putString("HELP_LAUNCHER", "OFF").apply()
            else
                prefs!!.edit().putString("HELP_LAUNCHER", "").apply()
        }
    }
}
