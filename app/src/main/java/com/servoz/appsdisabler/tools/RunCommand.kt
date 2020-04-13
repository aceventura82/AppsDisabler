package com.servoz.appsdisabler.tools

import android.content.Context
import android.widget.Toast
import com.servoz.appsdisabler.R
import java.io.*
import java.net.DatagramSocket
import java.net.Socket


class RunCommand {

    fun sudoForResult(vararg strings: String): String? {
        var res: String? = ""
        var outputStream: DataOutputStream? = null
        var response: InputStream? = null
        try {
            val su = Runtime.getRuntime().exec("su")
            outputStream = DataOutputStream(su.outputStream)
            response = su.inputStream
            for (s in strings) {
                outputStream.writeBytes("$s\n")
                outputStream.flush()
            }
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            try {
                su.waitFor()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            res = readFully(response)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            Closer.closeSilently(
                outputStream,
                response
            )
        }
        return res
    }

    @Throws(IOException::class)
    private fun readFully(`is`: InputStream?): String? {
        val byteArrOS = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int
        while (`is`!!.read(buffer).also { length = it } != -1) {
            byteArrOS.write(buffer, 0, length)
        }
        return byteArrOS.toString("UTF-8")
    }

    private object Closer {
        // closeAll()
        fun closeSilently(vararg xs: Any?) {
            // Note: on Android API levels prior to 19 Socket does not implement Closeable
            for (x in xs) {
                if (x != null) {
                    try {
                        when(x){
                            is Closeable ->{ (x).close() }
                            is Socket ->{ (x).close() }
                            is DatagramSocket -> { (x).close() }
                            else -> {
                                throw RuntimeException("cannot close $x") }
                        }
                    } catch (e: Throwable) {
                        println(e)
                    }
                }
            }
        }
    }

    fun enableApp(context: Context,app:ArrayList<String>){
        val objCmd= RunCommand()
        objCmd.sudoForResult("pm enable ${app[0]}")
        Toast.makeText(context, "${app[1]} ${context.getString(R.string.enabled)}", Toast.LENGTH_SHORT).show()
    }

    fun disableApp(context: Context,app:ArrayList<String>){
        val objCmd= RunCommand()
        objCmd.sudoForResult("pm disable ${app[0]}")
        Toast.makeText(context, "${app[1]} ${context.getString(R.string.disabled)}", Toast.LENGTH_SHORT).show()
    }
}