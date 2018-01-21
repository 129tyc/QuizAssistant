package com.ychtan.quizassistant.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ProgressBar
import android.widget.TextView
import com.ychtan.quizassistant.R
import kotlin.math.roundToInt

/**
 * Created by 谈永成 on 2018/1/15.
 */
class ResultAdapter(result: Map<String, Double>,
                    context: Context) : BaseAdapter() {
    private val showList = result.toList().map { return@map it.first to (it.second * 100).roundToInt() }
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    class ViewHolder(val options: TextView,
                     val value: TextView,
                     val relativity: ProgressBar)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.item_list_result, null)
            holder = ViewHolder(view.findViewById(R.id.tv_result),
                    view.findViewById(R.id.tv_value),
                    view.findViewById(R.id.pb_result))
            view.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            view = convertView
        }
        holder.options.text = showList[position].first
        holder.value.text = showList[position].second.toString() + "%"
        holder.relativity.progress = showList[position].second
        return view
    }

    override fun getItem(position: Int): Any = showList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = showList.size
}