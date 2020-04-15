package com.servoz.appsdisabler.config

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.RecyclerView
import com.servoz.appsdisabler.R
import com.servoz.appsdisabler.tools.RunCommand
import com.servoz.appsdisabler.tools.Db
import com.servoz.appsdisabler.tools.DynamicSearchAdapter
import kotlinx.android.synthetic.main.item_apps_layout.view.*
import org.jetbrains.anko.doAsync


class AppsListRecyclerAdapter(private val dataList: MutableList<SearchApps>) : DynamicSearchAdapter<SearchApps>(dataList) {

    private lateinit var fragment: Fragment

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_apps_layout, parent, false)
        fragment=parent.findFragment()
        return ViewHolder(
            v
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        ViewHolder(holder.itemView)
            .bindItems(dataList[position].data,fragment)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var prefFile: String = "com.servoz.appsdisabler.prefs"
        private var prefs: SharedPreferences? = null

        fun bindItems(data: ArrayList<String>, fragment: Fragment) {
            prefs = fragment.requireContext().getSharedPreferences(prefFile, 0)
            val dbHandler = Db(fragment.requireContext(), null)
            val appData=dbHandler.getData("app", "id='${data[0]}'")
            val objCmd= RunCommand()
            val tag=if(fragment.requireActivity().intent.getStringExtra("TAG")!=null)
                "|"+fragment.requireActivity().intent.getStringExtra("TAG")+"|"
                else ""

            drawView(fragment,appData, data)
            if(prefs!!.getString("SHOW_TABS","")=="ON")
                setTags(appData)
            setColors(fragment, data, appData)
            disableClick(fragment, data, objCmd)
            launcherClick(fragment, dbHandler, data, tag)
            gridClick(fragment, dbHandler, data, objCmd, tag)
            gridLongClick(fragment.requireContext(), itemView.textItemAppName, objCmd, dbHandler, data)
        }

        private fun drawView(fragment: Fragment, appData: ArrayList<ArrayList<String>>, data: ArrayList<String>){
            try {
                val icon: Drawable = fragment.requireContext().packageManager.getApplicationIcon(data[0])
                itemView.imageItemAppIcon.setImageDrawable(icon)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            itemView.switchAppsStatus.isChecked = !fragment.requireActivity().packageManager.getApplicationInfo(data[0], 0).enabled
            itemView.switchAppsLauncher.isChecked = appData.count()==1
            itemView.textItemAppName.text=data[1]
        }

        private fun setTags(appData: ArrayList<ArrayList<String>>){
            if(appData.count()==1 && appData[0][3]!=""){
                var tags=""
                for (i in 0..10){
                    val t=prefs!!.getString("TAG_$i","")
                    if(t!="" && appData[0][3].contains("|TAG_$i|")){
                        tags+="$t,"
                    }
                }
                tags=if(tags=="")"" else tags.substring(0,tags.length-1)
                itemView.textItemTags.text=tags
            }else
                itemView.textItemTags.text=""
        }

        private fun setColors(fragment: Fragment, data: ArrayList<String>, appData: ArrayList<ArrayList<String>>){
            //app in DB
            defaultBG(itemView.gridItemMyGames)
            if(appData.count()==1){
                itemView.textItemAppName.setTextColor( fragment.requireContext().getColor(R.color.design_default_color_on_primary))
                if(appData[0][2]!="1")
                    itemView.gridItemMyGames.setBackgroundColor( fragment.requireContext().getColor(R.color.colorConfigApp))
            } else
                itemView.textItemAppName.setTextColor(Color.GRAY)
            val mask = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
            val pm = fragment.requireContext().packageManager.getApplicationInfo(data[0] ,0)
            if(pm.flags and mask != 0)
                itemView.textItemAppName.setTextColor(Color.RED)
        }

        private fun disableClick(fragment: Fragment, data: ArrayList<String>, objCmd:RunCommand){
            itemView.switchAppsStatus.setOnClickListener{
                if(itemView.switchAppsStatus.isChecked)
                    objCmd.disableApp(fragment.requireContext(),data)
                else {
                    objCmd.enableApp(fragment.requireContext(), data)
                }
            }
        }

        private fun launcherClick(fragment: Fragment, dbHandler: Db, data: ArrayList<String>, tag:String){
            itemView.switchAppsLauncher.setOnClickListener{
                if(itemView.switchAppsLauncher.isChecked){
                    dbHandler.addData("app", hashMapOf("id" to data[0], "name" to data[1], "launcher" to "1", "tag" to tag) )
                    itemView.textItemAppName.setTextColor( fragment.requireContext().getColor(R.color.design_default_color_on_primary))
                }
                else{
                    dbHandler.deleteById("app", data[0])
                    defaultBG(itemView.gridItemMyGames)
                    itemView.textItemAppName.setTextColor(fragment.requireContext().getColor(R.color.colorAppDisabled))
                }
            }
        }

        private fun gridClick(fragment: Fragment, dbHandler: Db, data: ArrayList<String>, objCmd:RunCommand, tag:String){
            itemView.gridItemMyGames.setOnClickListener {
                if(itemView.switchAppsLauncher.isChecked){
                    dbHandler.deleteById("app", data[0])
                    objCmd.enableApp(fragment.requireContext(),data)
                    itemView.switchAppsStatus.isChecked = false
                    itemView.switchAppsLauncher.isChecked = false
                    itemView.textItemTags.text=""
                    defaultBG(itemView.gridItemMyGames)
                    itemView.textItemAppName.setTextColor(fragment.requireContext().getColor(R.color.colorAppDisabled))
                }
                else{
                    dbHandler.addData("app", hashMapOf("id" to data[0], "name" to data[1], "tag" to tag) )
                    objCmd.disableApp(fragment.requireContext(),data)
                    itemView.switchAppsStatus.isChecked = true
                    itemView.switchAppsLauncher.isChecked = true
                    itemView.textItemAppName.setTextColor( fragment.requireContext().getColor(R.color.design_default_color_on_primary))
                    itemView.gridItemMyGames.setBackgroundColor(fragment.requireContext().getColor(R.color.colorConfigApp))
                }
            }
        }

        private fun defaultBG(view: View){
            val baseColor = if(prefs!!.getString("BG","")=="")
                Color.BLACK
            else
                Color.parseColor(prefs!!.getString("BG",""))
            view.setBackgroundColor(baseColor)
            view.background.alpha=(prefs!!.getInt("ALPHA",50) / 100f * 255).toInt()
        }

        private fun gridLongClick(context: Context, view: View, objCmd: RunCommand, dbHandler:Db,data: ArrayList<String>) {
            itemView.gridItemMyGames.setOnLongClickListener{
                val popup = PopupMenu(context, view)
                popup.inflate(R.menu.menu_tags)
                if(prefs!!.getString("SHOW_TABS","")==""){
                    Toast.makeText(context, context.getString(R.string.tags_disabled), Toast.LENGTH_SHORT).show()
                    return@setOnLongClickListener true
                }
                val app=dbHandler.getData("app", "id='${data[0]}'")
                var hideMenu=true
                val menus= arrayListOf(R.id.tabList0,R.id.tabList1,R.id.tabList2,R.id.tabList3,R.id.tabList4,R.id.tabList5,R.id.tabList6,R.id.tabList7,R.id.tabList8,R.id.tabList9)
                for (i in 0 until 10){
                    val tag=prefs!!.getString("TAG_$i","")!!
                    val tagId="|TAG_$i|"
                    val mm = popup.menu.findItem(menus[i])
                    if(tag!=""){
                        if(app.count()==1 && !app[0][3].isBlank() && app[0][3].contains(tagId)){
                            mm.title = "$tag ✔"
                        }else
                            mm.title = tag
                        mm.setOnMenuItemClickListener {
                            val tags:String
                            val msg:String
                            //add app if not exits
                            val appEdit=if(app.count()==0){
                                dbHandler.addData("app", hashMapOf("id" to data[0], "name" to data[1], "tag" to tag) )
                                val appAux=arrayListOf(data[0],data[1],"",tag)
                                objCmd.disableApp(context,appAux)
                                itemView.switchAppsStatus.isChecked = true
                                itemView.switchAppsLauncher.isChecked = true
                                itemView.textItemAppName.setTextColor( context.getColor(R.color.design_default_color_on_primary))
                                itemView.gridItemMyGames.setBackgroundColor(context.getColor(R.color.colorConfigApp))
                                appAux
                            }else
                                app[0]
                            if(!appEdit[3].isBlank() && appEdit[3].contains(tagId)){
                                tags=appEdit[3].replace(tagId,"")
                                msg=context.getString(R.string.RemovedToTag, tag)
                                itemView.textItemTags.text=itemView.textItemTags.text.toString()
                                    .replace(",$tag", "").replace("$tag,", "").replace(tag, "")
                            }else{
                                tags=appEdit[3]+tagId
                                msg=context.getString(R.string.AddedToTag, tag)
                                val tTag=if(itemView.textItemTags.text=="") tag else ",$tag"
                                itemView.textItemTags.text=context.getString(R.string.text_append_tag, itemView.textItemTags.text, tTag)
                            }
                            dbHandler.editData("`app`","`id`='${appEdit[0]}'", hashMapOf("`tag`" to tags) )
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            true
                        }
                        hideMenu=false
                    }else
                        mm.isVisible=false
                }
                if(hideMenu){
                    Toast.makeText(context, context.getString(R.string.tags_disabled), Toast.LENGTH_SHORT).show()
                    return@setOnLongClickListener true
                }
                popup.show()
                true
            }
        }

    }
}

class SearchApps(val data: ArrayList<String>) : DynamicSearchAdapter.Searchable {

    override fun getSearchCriteria(): String {
        return if (data.count()>1) data[1] else ""
    }
}
