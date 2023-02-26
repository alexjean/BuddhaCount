package com.tomorrow_eyes.buddhacount.recordInterface;

import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.PopupMenu;


import com.tomorrow_eyes.buddhacount.ItemContent;
import com.tomorrow_eyes.buddhacount.MyItemRecyclerViewAdapter;
import com.tomorrow_eyes.buddhacount.MyViewModel;
import com.tomorrow_eyes.buddhacount.R;
import com.tomorrow_eyes.buddhacount.databinding.FragmentRecordBinding;

public interface LongClickItemHelper {

    Context getContext();
    String getString(int resId);
    void snackbarWarning(String msg, boolean warning);  // from other interface

    default void attachRecyclerViewItemLongClick(
            MyItemRecyclerViewAdapter adapter, MyViewModel viewModel, FragmentRecordBinding binding) {
        final Context _context = getContext();
        if (_context == null ) return;
        if (viewModel == null) return;
        if (binding == null) return ;
        final MyViewModel _viewModel = viewModel;
        final FragmentRecordBinding _binding = binding;
        adapter.setOnRecyclerViewItemLongClickListener((position, itemView) -> {
            ItemContent.CountItem countItem = ItemContent.ITEMS.get(position);
            Drawable background = itemView.getBackground();
            itemView.setBackgroundColor(Color.LTGRAY);
            PopupMenu popupMenu = new PopupMenu(_context, itemView);
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.popup_copy_title) {
                    _viewModel.setTitle(countItem.getContent());
                    _binding.editTextTitle.setText(_viewModel.getTitle());
                    _viewModel.writeConfig(_context);
                    return true;
                } else if (item.getItemId() == R.id.popup_bring_front) {
                    if (_viewModel.getCount() != 0)
                    {
                        snackbarWarning(getString(R.string.msg_count_number_not_zero), true);
                        return false;
                    }
                    _viewModel.setTitle(countItem.getContent());
                    _viewModel.setCount(countItem.getCount());
                    _viewModel.setMark(countItem.getMark());
                    _binding.editTextTitle.setText(_viewModel.getTitle());
                    _binding.textViewCount.setText(_viewModel.getCountString());
                    _viewModel.writeConfig(_context);
                    _viewModel.writeCountToFile(_context);
                    ItemContent.ITEMS.remove(position);
                    ItemContent.writeToFile(_context);
                    adapter.notifyItemRemoved(position);
                    return true;
                }
                return false;
            });
            popupMenu.setOnDismissListener(menu -> itemView.setBackground(background));
            Vibrator vibrator = (Vibrator) _context.getSystemService(Service.VIBRATOR_SERVICE);
            popupMenu.show();
            vibrator.vibrate(VibrationEffect.createOneShot(150, 200));
        });
    }

}
