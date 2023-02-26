package com.tomorrow_eyes.buddhacount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ItemListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)
        if (view is RecyclerView) {
            val context = view.getContext()
            view.layoutManager = LinearLayoutManager(context)
            val adapter = MyItemRecyclerViewAdapter(ItemContent.ITEMS)
            view.adapter = adapter
        }
        return view
    }

    companion object {
        fun newInstance(): ItemListFragment {
            val fragment = ItemListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}