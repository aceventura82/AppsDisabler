package com.servoz.appsdisabler

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import com.servoz.appsdisabler.tools.Db
import com.servoz.appsdisabler.tools.Notifications
import com.servoz.appsdisabler.tools.RunCommand
import java.lang.Thread.sleep
import java.util.*


class MyJobService : JobService() {
    private var jobCancelled = false
    override fun onStartJob(params: JobParameters): Boolean {
        println( "Job started")
        doBackgroundWork(params)
        return true
    }

    private fun doBackgroundWork(params: JobParameters) {
        Thread(Runnable {
            if (jobCancelled)
                return@Runnable
            changeAll(baseContext)
            println( "Job finished")
            jobFinished(params, false)
        }).start()
    }

    override fun onStopJob(params: JobParameters): Boolean {
        println( "Job cancelled before completion")
        jobCancelled = true
        return true
    }

    companion object {
        private const val TAG = "ExampleJobService"
    }

    private fun changeAll(context: Context){
        val objCmd= RunCommand()
        val dbHandler = Db(context, null)
        val apps=dbHandler.getData("app", "`launcher` != '1'")
        apps.sortBy{it[1].toLowerCase(Locale.ROOT)}
        println("DISABLER_DEBUG::apps to auto disable ${apps.count()}")
        var disableCount=0
        var disableInfo=""
        for(app in apps){
            if(context.packageManager.getApplicationInfo(app[0], 0).enabled){
                println("DISABLER_DEBUG::pm disable ${app[0]}")
                objCmd.sudoForResult("pm disable ${app[0]}")
                disableCount++
                disableInfo+=app[1]+", "
            }
        }
        if(disableCount>0){
            Notifications().create(context,"Disabler",context.getString(R.string.apps_disabled, disableCount.toString(), disableInfo.substring(0,disableInfo.length-2)), "SILENT")
            context.getSharedPreferences("com.servoz.appsdisabler.prefs", 0).edit().putString("RECREATE", "YES").apply()
        }
    }
}