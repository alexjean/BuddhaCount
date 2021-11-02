package com.tomorrow_eyes.buddhacount;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


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
            recyclerView.setAdapter(new MyItemRecyclerViewAdapter(ItemContent.ITEMS));

            mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                @Override
                public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                        final int swipeFlags = 0;
                        return makeMovementFlags(dragFlags, swipeFlags);
                    } else {
                        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                        final int swipeFlags = 0;
//                    final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                        return makeMovementFlags(dragFlags, swipeFlags);
                    }
                }

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
/*
                    int fromPosition = viewHolder.getAbsoluteAdapterPosition();
                    int toPosition = target.getAbsoluteAdapterPosition();
                    if (fromPosition < toPosition) {
                        for (int i = fromPosition; i < toPosition; i++) {
                            Collections.swap(datas, i, i + 1);
                        }
                    } else {
                        for (int i = fromPosition; i > toPosition; i--) {
                            Collections.swap(datas, i, i - 1);
                        }
                    }
                    myAdapter.notifyItemMoved(fromPosition, toPosition);
*/
                    return true;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAbsoluteAdapterPosition();
//                myAdapter.notifyItemRemoved(position);
//                datas.remove(position);
                }

                @Override
                public boolean isLongPressDragEnabled() {
                    return false;
                }

                @Override
                public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                    if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                        viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
                    }
                    super.onSelectedChanged(viewHolder, actionState);
                }

                @Override
                public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                    viewHolder.itemView.setBackgroundColor(0);
                }
            });
            mItemTouchHelper.attachToRecyclerView(recyclerView);


        }
        return view;
    }
}