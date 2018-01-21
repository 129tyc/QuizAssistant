package com.ychtan.quizassistant.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import com.ychtan.quizassistant.R

/**
 * Created by 谈永成 on 2018/1/14.
 */
class CheckAdapter(private val checkTextList: List<String>,
                   context: Context,
                   private val checkList: MutableList<Boolean>) : BaseAdapter() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    class ViewHolder(val check: CheckBox)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.item_list_assit, null)
            holder = ViewHolder(view.findViewById(R.id.cb_check))
            view.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            view = convertView
        }
        holder.check.text = checkTextList[position]
        if (position < checkList.size)
            holder.check.isChecked = checkList[position]
        holder.check.setOnCheckedChangeListener({ _, p1 ->
            if (position < checkList.size)
                checkList[position] = p1
        })
        return view
    }

    override fun getItem(position: Int): Any = checkTextList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = checkTextList.size
}

