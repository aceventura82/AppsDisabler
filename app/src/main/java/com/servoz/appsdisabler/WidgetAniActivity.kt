package com.servoz.appsdisabler

import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.servoz.appsdisabler.tools.Db
import com.servoz.appsdisabler.tools.RunCommand
import kotlinx.android.synthetic.main.widget_ani_layout.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Thread.sleep
import java.util.*
import kotlin.collections.ArrayList

class WidgetAniActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_ani_layout)
        widget_outer_layout.setOnClickListener {
            finishAfterTransition()
        }
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val tasks = am.appTasks
        if (tasks != null && tasks.size > 0) {
            tasks[0].setExcludeFromRecents(true)
        }
        if(intent.getStringExtra("DISABLE_ALL")=="YES")
            widgetAni()
        else if(intent.getStringExtra("ENABLE_ALL")=="YES")
            widgetAni(true)
    }

    private fun widgetAni(enable: Boolean=false){
        doAsync {
            val dbHandler = Db(this@WidgetAniActivity, null)
            val apps=dbHandler.getData("app", "`launcher` IS NULL")
            val msg: String
            val appsDisable= arrayListOf<ArrayList<String>>()
            for(app in apps)
                if(packageManager.getApplicationInfo(app[0], 0).enabled != enable)
                    appsDisable.add(app)
            if(appsDisable.count()==0) {
                msg =getString(R.string.no_apps_to_disable)
            }else {
                doAsync {
                    changeAll(this@WidgetAniActivity, enable)
                }
                for (app in appsDisable){
                    sleep(300)
                    uiThread {
                        dragApp(app[0], enable)
                    }
                }
                msg = getString(R.string.n_apps, appsDisable.count().toString(), if(enable)getString(R.string.enabled)else getString(R.string.disabled))
                getSharedPreferences("com.servoz.appsdisabler.prefs", 0).edit().putString("RECREATE", "YES").apply()
            }
            uiThread {
                widget_ani_text.isVisible=true
                widget_ani_text.setBackgroundColor(Color.parseColor("#60000000"))
                widget_ani_text.text= msg
            }
            sleep(2000)
            finishAfterTransition()
        }
    }

    private fun changeAll(context: Context, enable:Boolean=false){
        val objCmd= RunCommand()
        val dbHandler = Db(context, null)
        val apps=dbHandler.getData("app", "`launcher` IS NULL")
        apps.sortBy{it[1].toLowerCase(Locale.ROOT)}
        for(app in apps)
            objCmd.sudoForResult("pm ${if(enable)"enable" else "disable"} ${app[0]}")
    }

    private fun dragApp(app:String, enable:Boolean){
        val img = ImageView(this)
        img.setImageDrawable(packageManager.getApplicationIcon(app))
        img.layoutParams=LinearLayout.LayoutParams(80.dp, 80.dp).apply {
            setMargins(0,0,0,0)
        }
        if(enable)
            setGrayApp(img)
        img.setPadding(0,0,0,0)
        widget_layout.addView(img)

        val animate = TranslateAnimation(
            0F, 0F,
            100F, resources.displayMetrics.heightPixels.toFloat()-100)
        animate.duration = 300
        doAsync {
            sleep(150)
            uiThread {
                if(!enable)
                    setGrayApp(img)
                else{
                    img.colorFilter = null
                    img.imageAlpha = 255
                }
            }
        }
        img.startAnimation(animate)
        animate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                img.isVisible=false
            }
        })
    }

    private fun setGrayApp(v: ImageView) {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f) //0 means grayscale
        val cf = ColorMatrixColorFilter(matrix)
        v.colorFilter = cf
        v.imageAlpha = 128 // 128 = 0.5
    }

}
