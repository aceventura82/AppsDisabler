package com.servoz.appsdisabler.config

import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.RecyclerView
import com.servoz.appsdisabler.R
import com.servoz.appsdisabler.tools.RunCommand
import com.servoz.appsdisabler.tools.Db
import com.servoz.appsdisabler.tools.DynamicSearchAdapter
import kotlinx.android.synthetic.main.item_apps_layout.view.*


class AppsListRecyclerAdapter(private val dataList: MutableList<SearchApps>) : DynamicSearchAdapter<SearchApps>(dataList) {

    private lateinit var fragment: Fragment
    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_apps_layout, parent, false)
        fragment=parent.findFragment()
        return ViewHolder(
            v
        )
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        ViewHolder(holder.itemView)
            .bindItems(dataList[position].data,fragment)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return dataList.size
    }

    //the class holding the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(data: ArrayList<String>, fragment: Fragment) {
            try {
                val icon: Drawable = fragment.requireContext().packageManager.getApplicationIcon(data[0])
                itemView.imageItemAppIcon.setImageDrawable(icon)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            val dbHandler = Db(fragment.requireContext(), null)
            val appData=dbHandler.getData("app", "id='${data[0]}'")
            val objCmd= RunCommand()
            itemView.textItemAppName.text=data[1]
            itemView.switchAppsStatus.isChecked = !fragment.requireActivity().packageManager.getApplicationInfo(data[0], 0).enabled
            itemView.switchAppsLauncher.isChecked = appData.count()==1
            val defBG=itemView.itemLayout.background
            if(appData.count()==1 && appData[0][2]!="1"){
                itemView.gridItemMyGames.setBackgroundColor( fragment.requireContext().getColor(R.color.colorAppSelected))
                itemView.textItemAppName.setTextColor( fragment.requireContext().getColor(R.color.design_default_color_on_primary))
            }
            else if(appData.count()==1) {
                itemView.textItemAppName.setTextColor( fragment.requireContext().getColor(R.color.design_default_color_on_primary))
                itemView.gridItemMyGames.background = defBG
            }
            else {
                itemView.textItemAppName.setTextColor(Color.RED)
                itemView.gridItemMyGames.background = defBG
            }
            itemView.switchAppsStatus.setOnClickListener{
                if(itemView.switchAppsStatus.isChecked)
                    objCmd.disableApp(fragment.requireContext(),data)
                else {
                    objCmd.enableApp(fragment.requireContext(), data)
                }
            }
            val tag=if(fragment.requireActivity().intent.getStringExtra("TAG")!=null)
                "|"+fragment.requireActivity().intent.getStringExtra("TAG")+"|"
                else ""
            itemView.switchAppsLauncher.setOnClickListener{
                if(itemView.switchAppsLauncher.isChecked){
                    dbHandler.addData("app", hashMapOf("id" to data[0], "name" to data[1], "launcher" to "1", "tag" to tag) )
                    itemView.textItemAppName.setTextColor( fragment.requireContext().getColor(R.color.design_default_color_on_primary))
                }
                else{
                    dbHandler.deleteById("app", data[0])
                    itemView.gridItemMyGames.background=defBG
                    itemView.textItemAppName.setTextColor( Color.RED)
                }
            }
            itemView.gridItemMyGames.setOnClickListener {
                if(itemView.switchAppsLauncher.isChecked){
                    dbHandler.deleteById("app", data[0])
                    objCmd.enableApp(fragment.requireContext(),data)
                    itemView.switchAppsStatus.isChecked = false
                    itemView.switchAppsLauncher.isChecked = false
                    itemView.gridItemMyGames.background= defBG
                    itemView.textItemAppName.setTextColor(Color.RED)
                }
                else{
                    dbHandler.addData("app", hashMapOf("id" to data[0], "name" to data[1], "tag" to tag) )
                    objCmd.disableApp(fragment.requireContext(),data)
                    itemView.switchAppsStatus.isChecked = true
                    itemView.switchAppsLauncher.isChecked = true
                    itemView.textItemAppName.setTextColor( fragment.requireContext().getColor(R.color.design_default_color_on_primary))
                    itemView.gridItemMyGames.setBackgroundColor(fragment.requireContext().getColor(R.color.colorAppSelected))
                }
            }
        }
    }
}

class SearchApps(val data: ArrayList<String>) : DynamicSearchAdapter.Searchable {

    override fun getSearchCriteria(): String {
        return if (data.count()>1) data[1] else ""
    }
}
