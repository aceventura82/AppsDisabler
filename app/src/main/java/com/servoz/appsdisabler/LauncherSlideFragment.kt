package com.servoz.appsdisabler

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.servoz.appsdisabler.tools.Db
import com.servoz.appsdisabler.tools.RunCommand
import kotlinx.android.synthetic.main.slide_fragment.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.collections.ArrayList


class LauncherSlideFragment : Fragment() {
    private var textColor=0
    private var textColor2=0
    private var labels=""
    private var tags=ArrayList<String>()
    private var grayIcons=false
    private var tagsOrder=ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
        Bundle?): View? {
        val view = inflater.inflate(R.layout.slide_fragment, container, false)
        // Retrieve and display the data from the Bundle
        val tag = requireArguments().getString("tag")!!
        textColor = requireArguments().getInt("color1")
        textColor2 = requireArguments().getInt("color2")
        labels = requireArguments().getString("labels")!!
        tags= requireArguments().getStringArrayList("tags") as ArrayList<String>
        grayIcons = requireArguments().getString("grayIcons")!! == "ON"
        tagsOrder= requireArguments().getStringArrayList("tagsOrder") as ArrayList<String>
        getApps(view, requireArguments().getInt("cols"),tag)
        return view
    }

    private fun getApps(view:View, cols:Int, tag:String=""){
        val objCmd= RunCommand()
        val dbHandler = Db(requireContext(), null)
        var col=0
        var row=0
        val apps=checkUninstalledApps(tag, dbHandler)
        val prefs = requireActivity().getSharedPreferences("com.servoz.appsdisabler.prefs", 0)
        apps.sortBy{it[1].toLowerCase(Locale.ROOT)}
        val columns=if (apps.count() in 1 until cols) apps.count() else cols
        view.gridMyApps.removeAllViews()
        view.gridMyApps.columnCount=columns
        val cellSize = (resources.displayMetrics.widthPixels-10.dp)/cols
        for((c, app) in apps.withIndex()){
            createAppView(view, tag, app, cellSize, objCmd, row, col, dbHandler, c, prefs.getInt("ICONS_SIZE", 40))
            col++
            if(col >0 && col.rem(columns)==0){
                col=0
                row++
            }
        }
    }
    
    private fun checkUninstalledApps(tag: String, dbHandler: Db):ArrayList<ArrayList<String>>{
        val appsVal=dbHandler.getData("app", if(tag!="")"`tag` like '%|$tag|%'" else "")
        var valDel = false
        for(app in appsVal) {
            try {
                val pm = requireActivity().packageManager
                pm.getPackageInfo(app[0], PackageManager.GET_ACTIVITIES)
            } catch (e: PackageManager.NameNotFoundException) {
                dbHandler.deleteById("app",app[0])
                valDel=true
            }
        }
        return if(valDel)dbHandler.getData("app", if(tag!="")"`tag`like'%|$tag|%'" else "") else appsVal
    }

    private fun createAppView(view: View, tag:String, app: ArrayList<String>, cellSize: Int, objCmd: RunCommand, row:Int, col:Int, dbHandler: Db, c:Int, imgSize:Int){
        doAsync {
            val ll = LinearLayout(requireContext())
            ll.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            ll.orientation = LinearLayout.VERTICAL

            val img = ImageView(requireContext())
            img.id = 10000+c
            img.setImageDrawable(requireActivity().packageManager.getApplicationIcon(app[0]))
            val iconSize=if(imgSize==10) cellSize-(cellSize.div(5)) else imgSize
            img.layoutParams=LinearLayout.LayoutParams(iconSize, iconSize).apply {
                setMargins(0,10,0,10)
            }
            img.setPadding(0,0,0,0)

            val text = TextView(requireContext())
            val dataTitle= if (app[1].length> 10) app[1].substring(0,10)+".." else app[1]
            if(labels=="ON"){
                if(tag=="" && (app[3]=="||" || app[3]==""))
                    text.paintFlags= Paint.UNDERLINE_TEXT_FLAG
                text.text= dataTitle
                text.id= c
                text.setTextColor( if(requireActivity().packageManager.getApplicationInfo(app[0], 0).enabled)textColor else textColor2)
                text.maxLines=1
                text.layoutParams=LinearLayout.LayoutParams(
                    cellSize,LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0,0,0,10)
                }
                text.gravity= Gravity.CENTER_HORIZONTAL
                text.setTypeface(null, Typeface.BOLD)
                text.setPadding(0,0,0,0)
            }
            if(grayIcons && !requireActivity().packageManager.getApplicationInfo(app[0], 0).enabled)
                setGrayApp(img)
            ll.addView(img)
            ll.addView(text)
            ll.gravity=Gravity.CENTER
            ll.setPadding(0,0,0,0)
            ll.layoutParams=GridLayout.LayoutParams(ViewGroup.LayoutParams(cellSize, GridLayout.LayoutParams.WRAP_CONTENT)).apply {
                columnSpec=GridLayout.spec(col)
                rowSpec=GridLayout.spec(row)
                setMargins(0,0,0,0)
            }
            uiThread {
                view.findViewById<GridLayout>(R.id.gridMyApps).addView(ll)
                ll.setOnClickListener {
                    launchApp(objCmd, app)
                    text.setTextColor(textColor)
                    restoreColor(img)
                }
                //menu
                ll.setOnLongClickListener {
                    showAppMenu(ll, objCmd, dbHandler, app, text)
                    true
                }

            }
        }
    }
    
    private fun launchApp(objCmd:RunCommand, app: ArrayList<String>) {
        if(!requireActivity().packageManager.getApplicationInfo(app[0], 0).enabled)
            objCmd.enableApp(requireContext(), app)
        try{
            val intent: Intent = requireActivity().packageManager.getLaunchIntentForPackage(app[0])!!
            startActivity(intent)
        }catch (ex:KotlinNullPointerException){
            Toast.makeText(requireContext(), getString(R.string.root_needed), Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showAppMenu(view: View, objCmd: RunCommand, dbHandler:Db, app: ArrayList<String>, text:TextView) {
        val popup = PopupMenu(requireContext(), view)
        popup.inflate(R.menu.menu_app)
        if(app[2]!="1") {
            popup.menu.findItem(R.id.menuAddApp).isVisible = false
            popup.menu.findItem(R.id.menuRemoveAutoApp).isVisible=true
        }else{
            popup.menu.findItem(R.id.menuRemoveAutoApp).isVisible=false
            popup.menu.findItem(R.id.menuAddApp).isVisible = true
        }
        popup.menu.findItem(R.id.menuEnableApp).isVisible=false
        popup.menu.findItem(R.id.menuDisableApp).isVisible=false
        if(grayIcons && !requireActivity().packageManager.getApplicationInfo(app[0], 0).enabled)
            popup.menu.findItem(R.id.menuEnableApp).isVisible=true
        else
            popup.menu.findItem(R.id.menuDisableApp).isVisible=true

        var hideMenu=true
        val menus= arrayListOf(R.id.tab0,R.id.tab1,R.id.tab2,R.id.tab3,R.id.tab4,R.id.tab5,R.id.tab6,R.id.tab7,R.id.tab8,R.id.tab9)
        for (i in 0 until 10){
            if(tags.count()==0)
                break
            val tag=tags[i]
            val tagId="|TAG_${tagsOrder[i]}|"
            val mm = popup.menu.findItem(menus[i])
            if(tag!=""){
                if(!app[3].isBlank() && app[3].contains(tagId)){
                    mm.title="$tag ✔"
                }else
                     mm.title = tag
                mm.setOnMenuItemClickListener {
                    val tagsList:String
                    val msg:String
                    if(!app[3].isBlank() && app[3].contains(tagId)){
                        tagsList=app[3].replace(tagId,"")
                        msg=getString(R.string.RemovedToTag, tag)
                    }else{
                        tagsList=app[3]+tagId
                        msg=getString(R.string.AddedToTag, tag)
                    }
                    requireActivity().recreate()
                    dbHandler.editData("app","`id`='${app[0]}'", hashMapOf("`tag`" to tagsList) )
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    true
                }
                hideMenu=false
            }else
                mm.isVisible=false
        }
        if(hideMenu)
            popup.menu.findItem(R.id.menuTag).isVisible=false

        popup.setOnMenuItemClickListener{ item: MenuItem? ->
            when (item!!.itemId) {
                R.id.menuRemoveApp -> {
                    objCmd.enableApp(requireContext(),app)
                    dbHandler.deleteById("app", app[0])
                    requireActivity().recreate()
                }
                R.id.menuAddApp -> {
                    objCmd.disableApp(requireContext(),app)
                    dbHandler.editData("app", "`id`='${app[0]}'", hashMapOf("launcher" to ""))
                    text.setTextColor(textColor2)
                    requireActivity().recreate()
                }
                R.id.menuRemoveAutoApp -> {
                    objCmd.disableApp(requireContext(),app)
                    dbHandler.editData("app", "`id`='${app[0]}'", hashMapOf("launcher" to "1"))
                    text.setTextColor(textColor)
                    requireActivity().recreate()
                }
                R.id.menuEnableApp -> {
                    objCmd.enableApp(requireContext(), app)
                    text.setTextColor(textColor)
                    requireActivity().recreate()
                }
                R.id.menuDisableApp -> {
                    objCmd.disableApp(requireContext(), app)
                    text.setTextColor(textColor2)
                    requireActivity().recreate()
                }
                R.id.menuAppSettings -> {
                    showInstalledAppDetails(requireContext(), app[0])
                }
            }
            true
        }
        popup.show()
    }

    private fun showInstalledAppDetails(context: Context, packageName: String?) {
        val intent = Intent()
        val apiLevel = Build.VERSION.SDK_INT
        if (apiLevel >= 9) { // above 2.3
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
        } else { // below 2.3
            val appPkgName: String =
                if (apiLevel == 8) "pkg" else "com.android.settings.ApplicationPkgName"
            intent.action = Intent.ACTION_VIEW
            intent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails")
            intent.putExtra(appPkgName, packageName)
        }
        context.startActivity(intent)
    }

    private fun setGrayApp(v: ImageView) {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f) //0 means grayscale
        val cf = ColorMatrixColorFilter(matrix)
        v.colorFilter = cf
        v.imageAlpha = 128 // 128 = 0.5
    }

    private fun restoreColor(v: ImageView){
        v.colorFilter = null
        v.imageAlpha = 255
    }

    companion object {
        fun newInstance(apps:TagView): LauncherSlideFragment {
            val args = Bundle()
            args.putString("tag", apps.tag)
            args.putInt("cols", apps.cols)
            args.putInt("color1", apps.color1)
            args.putInt("color2", apps.color2)
            args.putString("labels", apps.labels)
            args.putStringArrayList("tags", apps.tags)
            args.putString("grayIcons", apps.grayIcons)
            args.putStringArrayList("tagsOrder", apps.tagsOrder)
            val fragment = LauncherSlideFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
