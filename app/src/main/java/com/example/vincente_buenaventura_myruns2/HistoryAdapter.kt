package com.example.vincente_buenaventura_myruns2

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import kotlin.math.roundToInt

class HistoryAdapter(private val context: Context, private var historyList: List<HistoryEntry>) : BaseAdapter() {
    override fun getItem(position: Int): Any {
        return historyList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return historyList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.layout_history, null)

        val textViewTop = view.findViewById(R.id.TopTextView) as TextView
        val textViewBottom = view.findViewById(R.id.BottomTextView) as TextView

        var str = ""
        var str2 = ""

        if (historyList.get(position).inputType == 0)
            str+="Manual Entry: "
        else if (historyList.get(position).inputType == 1)
            str+="GPS: "
        else
            str+="Automatic: "

        when (historyList.get(position).activityType) {
            0 -> str+="Running"
            1 -> str+="Walking"
            2 -> str+="Standing"
            3 -> str+="Cycling"
            4 -> str+="Hiking"
            5 -> str+="Downhill Skiing"
            6 -> str+="Cross-Country Skiing"
            7 -> str+="Snowboarding"
            8 -> str+="Skating"
            9 -> str+="Swimming"
            10 -> str+="Mountain Biking"
            11 -> str+="Wheelchair"
            12 -> str+="Elliptical"
            13 -> str+="Other"

    }



        str+=", "+convertMonth(historyList.get(position).dateTime)

        val sharedPreferences: SharedPreferences = context.getSharedPreferences("radioButtonPrefs", Context.MODE_PRIVATE)
        val savedRadioButtonId = sharedPreferences.getInt("selectedOption", -1)


        if (savedRadioButtonId == -1){
            str2+=historyList.get(position).distance.toString()+" miles, "
        }else if (savedRadioButtonId == 2131231127 && historyList.get(position).units == "miles"){
            str2+=(((historyList.get(position).distance/ 0.621371)* 100).roundToInt() / 100.0).toString()+" km, "
        }else if (savedRadioButtonId == 2131231128 && historyList.get(position).units == "kilometers"){
            str2+=(((historyList.get(position).distance* 0.621371)* 100).roundToInt() / 100.0).toString()+" miles, "
        }else{
            if (historyList.get(position).units == "miles"){
                str2+=((historyList.get(position).distance* 100).roundToInt() / 100.0).toString()+" miles, "
            }   else{
                str2+=((historyList.get(position).distance* 100).roundToInt() / 100.0).toString()+" km, "
            }
        }

        val durationVal = historyList.get(position).duration



        val minutes = kotlin.math.floor(durationVal).toInt()

        val temp = durationVal - minutes

        val seconds = (temp * 60).toInt()


        str2+=minutes.toString()+" mins "+seconds.toString()+" secs"

        textViewTop.text = str
        textViewBottom.text = str2



        return view

    }

    fun replace(newHistoryList: List<HistoryEntry>){

        historyList = newHistoryList
        notifyDataSetChanged()
    }

    fun convertMonth(month: String?): String{
        var temp = month?.split(" ")?.toMutableList()
        var temp2 = temp?.get(1)
        var tempMonth = ""
        when(temp2){
            "1" -> tempMonth = "Jan"
            "2" -> tempMonth = "Feb"
            "3" -> tempMonth = "Mar"
            "4" -> tempMonth = "Apr"
            "5" -> tempMonth = "May"
            "6" -> tempMonth = "Jun"
            "7" -> tempMonth = "Jul"
            "8" -> tempMonth = "Aug"
            "9" -> tempMonth = "Sep"
            "10" -> tempMonth = "Oct"
            "11" -> tempMonth = "Nov"
            "12" -> tempMonth = "Dec"
        }
        temp?.set(1, tempMonth)
        if (temp != null) {
            return temp.joinToString(" ")
        }
        return ""


    }


}