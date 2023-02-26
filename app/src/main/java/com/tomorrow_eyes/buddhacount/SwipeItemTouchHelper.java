package com.tomorrow_eyes.buddhacount;

import static androidx.recyclerview.widget.ItemTouchHelper.DOWN;
import static androidx.recyclerview.widget.ItemTouchHelper.END;
import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;
import static androidx.recyclerview.widget.ItemTouchHelper.START;
import static androidx.recyclerview.widget.ItemTouchHelper.UP;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public interface SwipeItemTouchHelper {
    @Nullable
    Context getContext();
    default void attachItemTouchHelper(RecyclerView recyclerView, RecyclerView.Adapter adapter)
    {
        ItemTouchHelper itemTouchHelper = new androidx.recyclerview.widget.ItemTouchHelper(new androidx.recyclerview.widget.ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    final int dragFlags = UP | DOWN | LEFT | RIGHT;
                    final int swipeFlags = 0;
                    return makeMovementFlags(dragFlags, swipeFlags);
                } else {
                    final int dragFlags = UP | DOWN;
                    final int swipeFlags = START | END;
                    return makeMovementFlags(dragFlags, swipeFlags);
                }
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAbsoluteAdapterPosition();
                ItemContent.CountItem item = ItemContent.ITEMS.get(position);
                AlertDialog.Builder dlg  = new AlertDialog.Builder(getContext());
                String title = item.getContent();
                if (title.length() > 7) title= title.substring(0, 7)+"..";
                dlg.setMessage(String.format("<%s> %s   %d", item.getId(), title, item.getCount()));
                dlg.setTitle(R.string.confirm_delete);
                dlg.setCancelable(true);
                dlg.setPositiveButton(R.string.confirm_text, (dialog, which) -> {
                    ItemContent.ITEMS.remove(position);
                    ItemContent.writeToFile(getContext());
                    adapter.notifyItemRemoved(position);
                });
                dlg.setNegativeButton(R.string.cancel_text, null);
                dlg.setOnDismissListener(dialog1 -> adapter.notifyItemChanged(position));
                dlg.create().show();
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


}
