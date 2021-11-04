package com.tomorrow_eyes.buddhacount;

import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;


public class ItemFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private ItemTouchHelper mItemTouchHelper;

    public ItemFragment() {
    }

    public static ItemFragment newInstance(int columnCount) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            MyItemRecyclerViewAdapter adapter = new MyItemRecyclerViewAdapter(ItemContent.ITEMS);
            recyclerView.setAdapter(adapter);

            // LongClickListener for 刪除
            adapter.setOnRecyclerViewItemLongClickListener((position, itemView) -> {
                Drawable background=itemView.getBackground();
                itemView.setBackgroundColor(Color.LTGRAY);
                PopupMenu popupMenu = new PopupMenu(context,itemView);
                popupMenu.getMenuInflater().inflate(R.menu.menu_popup,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    if(item.getItemId()==R.id.popup_delete) {
                        ItemContent.ITEMS.remove(position);
                        ItemContent.writeToFile(context);
                        adapter.notifyItemRemoved(position);
                        return true;
                    }
                    return false;
                });
                popupMenu.setOnDismissListener(menu -> itemView.setBackground(background));
                Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
                popupMenu.show();
                vibrator.vibrate(VibrationEffect.createOneShot(150,200));
            });

        }
        return view;
    }
}