package com.servoz.appsdisabler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.servoz.appsdisabler.tools.Db
import com.servoz.appsdisabler.tools.RunCommand
import org.jetbrains.anko.doAsync
import java.lang.Thread.sleep
import java.util.*


class MyBroadCastReceiver : BroadcastReceiver() {
    private var processId=0
    override fun onReceive(context: Context?, intent: Intent) {
        val timeout=(context!!.getSharedPreferences("com.servoz.appsdisabler.prefs", 0).getString("SCREEN_TIMEOUT", "-1")!!).toLong()
        println("IN $timeout")
        if(timeout==-1L)
            return
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            doAsync {
                processId= (1..99999999).random()
                val pp=processId
                sleep(timeout*60000)
                changeAll(context, pp)
            }
        } else if (intent.action == Intent.ACTION_SCREEN_ON) {
            processId=0
        }
    }

    private fun changeAll(context: Context, pId:Int){
        if(pId!=processId){
            return
        }
        val objCmd= RunCommand()
        val dbHandler = Db(context, null)
        val apps=dbHandler.getData("app")
        apps.sortBy{it[1].toLowerCase(Locale.ROOT)}
        for(app in apps){
            if(app[2] == "1")
                continue
            objCmd.sudoForResult("pm disable ${app[0]}")
        }
    }
}