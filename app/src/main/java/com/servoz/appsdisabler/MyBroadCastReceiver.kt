package com.servoz.appsdisabler

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Context.JOB_SCHEDULER_SERVICE
import android.content.Intent


class MyBroadCastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val timeout=(context!!.getSharedPreferences("com.servoz.appsdisabler.prefs", 0).getString("SCREEN_TIMEOUT", "-1")!!).toLong()
        if(timeout==-1L)
            return
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            println("DISABLER_DEBUG::SCREEN OFF")
            scheduleJob(context, timeout)
        } else if (intent.action == Intent.ACTION_SCREEN_ON) {
            println("DISABLER_DEBUG::SCREEN ON")
            val scheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler?
            scheduler!!.cancel(123)
        }
    }

    private fun scheduleJob(context: Context,time:Long) {
        val info = JobInfo.Builder(123, ComponentName(context, MyJobService::class.java))
            .setMinimumLatency(time*60*1000)
            .build()
        val scheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler?
        val resultCode = scheduler!!.schedule(info)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            println( "Job scheduled")
        } else {
            println( "Job scheduling failed")
        }
    }
}