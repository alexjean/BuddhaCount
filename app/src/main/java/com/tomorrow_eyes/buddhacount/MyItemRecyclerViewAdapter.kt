package com.tomorrow_eyes.buddhacount

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tomorrow_eyes.buddhacount.ItemContent.CountItem
import com.tomorrow_eyes.buddhacount.databinding.FragmentItemBinding
import java.time.format.DateTimeFormatter

class MyItemRecyclerViewAdapter(private val mValues: List<CountItem>) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FragmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val view: View = binding.root
        val viewHolder: ViewHolder = ViewHolder(binding)
        view.setOnLongClickListener { _: View? ->
            val position = viewHolder.layoutPosition
            //String msg = "Item<" + position + "> long Clicked!";
            //Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
            longClickListener?.onRecyclerViewItemLongClick(position, view)
            true
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mItem = item
        var str = item.id
        val l1 = str.length
        if (l1 < 2) str = "  ".substring(l1) + str
        holder.mIdView.text = str
        holder.mContentView.text = item.content
        str = item.count.toString()
        holder.mCountView.text = str
        val formatter = DateTimeFormatter.ofPattern("yy-MM-dd")
        holder.mMarkView.text = item.mark.format(formatter)
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val mIdView: TextView
        val mContentView: TextView
        val mCountView: TextView
        val mMarkView: TextView
        var mItem: CountItem? = null

        init {
            mIdView = binding.itemNumber
            mContentView = binding.content
            mCountView = binding.count
            mMarkView = binding.mark
        }

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }

    interface OnRecyclerViewItemLongClickListener {
        fun onRecyclerViewItemLongClick(position: Int, view: View?)
    }

    private var longClickListener: OnRecyclerViewItemLongClickListener? = null
    fun setOnRecyclerViewItemLongClickListener(longClickListener: OnRecyclerViewItemLongClickListener?) {
        this.longClickListener = longClickListener
    }
}