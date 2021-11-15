package com.tomorrow_eyes.buddhacount;

import static androidx.recyclerview.widget.ItemTouchHelper.Callback;
import static androidx.recyclerview.widget.ItemTouchHelper.DOWN;
import static androidx.recyclerview.widget.ItemTouchHelper.END;
import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;
import static androidx.recyclerview.widget.ItemTouchHelper.START;
import static androidx.recyclerview.widget.ItemTouchHelper.UP;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.tomorrow_eyes.buddhacount.ItemContent.CountItem;
import com.tomorrow_eyes.buddhacount.databinding.FragmentRecordBinding;

import java.time.LocalDate;
import java.util.List;

public class RecordFragment extends Fragment {

//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

//    private String mParam1;
//    private String mParam2;

    private FragmentRecordBinding binding;
    private MyViewModel viewModel;

    public RecordFragment() {
        // Required empty public constructor
    }

    public static RecordFragment newInstance() {
//        String param1, String param2) {
        RecordFragment fragment = new RecordFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_record, container, false);
        binding = FragmentRecordBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(getActivity()).get(MyViewModel.class);
        return binding.getRoot();
    }

    public void SnackbarWarning(View view, String msg , boolean warning) {
        Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
        View view2 = snackbar.getView();
        TextView tv = view2.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snackbar.show();
        if (warning) {
            Vibrator vibrator = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(350, 200));
        }
    }

    public void areYouOk(int gravity, String msg, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder  = new AlertDialog.Builder(getContext());
        builder.setMessage(msg);
        builder.setTitle(R.string.button_reset);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.confirm_text, onClickListener);
        builder.setNegativeButton(R.string.cancel_text, null);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setGravity(gravity);
        dialog.show();
    }

    private Context mContext;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ItemContent.readFromFile(mContext=getContext());
        binding.textViewCount.setText(viewModel.getCountString());
        binding.editTextTitle.setText(viewModel.getTitle(mContext));

        binding.buttonReset.setOnClickListener(view1 -> {
            String content = binding.editTextTitle.getText().toString();
            content=content.replace(",","");  // 不准有逗号
            content=content.replace("\n", " ").trim();
            binding.editTextTitle.setText(content);
            String msg = getString(R.string.msg_count_is_zero);
            if (!content.equals(viewModel.getTitle(mContext))) {
                msg = getString(R.string.msg_settting_title)+content;
                viewModel.setTitle(content);
                viewModel.writeConfig(mContext);
            }
            if (viewModel.getCount() == 0)
                SnackbarWarning(view, msg, true);
            else
                areYouOk(Gravity.CENTER, getString(R.string.msg_sure_to_record_than_reset), (dlg, which) -> {
                    if (ItemContent.sizeOver()) {
                        RecyclerView recyclerView = getSubRecyclerView();
                        if (recyclerView != null)
                            recyclerView.scrollToPosition(ItemContent.ITEMS.size()-1);
                        areYouOk(Gravity.BOTTOM, getString(R.string.msg_list_too_long_will_merge), (dlg1, which1) -> {
                            recordCountAdjustStatistic();
                            recyclerView.scrollToPosition(0);
                        });
                    }
                    else
                        recordCountAdjustStatistic();
                });
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        RecyclerView recyclerView = getSubRecyclerView();
        if (recyclerView == null) return;
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null) return;
        if (adapter instanceof MyItemRecyclerViewAdapter)
            attachRecyclerViewItemLongClick((MyItemRecyclerViewAdapter) adapter);
        attachItemTouchHelper(recyclerView, adapter);  // 在onViewCreated呼叫,recyclerView還是null
    }

    private RecyclerView getSubRecyclerView() {
        // FragmentContainerView containerView = binding.fragmentContainerView;
        // not support getFragment() until Fragment:1.4.0
        //ItemFragment itemFragment = (ItemFragment)containerView.getFragment();
        FragmentManager fragmentManager = getChildFragmentManager();
        List<Fragment> list = fragmentManager.getFragments();
        if (list.isEmpty()) return null;
        Fragment fragment = list.get(0);
        if (!(fragment instanceof ItemFragment)) return null;
        return (RecyclerView) fragment.getView();
    }

    public void recordCountAdjustStatistic() {
        String content = viewModel.getTitle(mContext);
        ItemContent.insertItemUpdateList(new CountItem("0", content,
                viewModel.getCount(), viewModel.getMark()));
        viewModel.setCount(0);
        viewModel.setMark(LocalDate.now());
        binding.textViewCount.setText(viewModel.getCountString());
        viewModel.writeCountToFile(mContext);
        RecyclerView recyclerView = getSubRecyclerView();
        if (recyclerView == null) return;
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter != null) adapter.notifyDataSetChanged();
        ItemContent.writeToFile(mContext);
        SnackbarWarning(binding.getRoot(), getString(R.string.msg_already_on_top), false);
    }

    public void attachItemTouchHelper(RecyclerView recyclerView, RecyclerView.Adapter adapter)
    {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    final int dragFlags = UP | DOWN |
                            LEFT | RIGHT;
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
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAbsoluteAdapterPosition();
                CountItem item = ItemContent.ITEMS.get(position);
                AlertDialog.Builder dlg  = new AlertDialog.Builder(mContext);
                String title = item.content;
                if (title.length() > 7) title= title.substring(0, 7)+"..";
                dlg.setMessage(String.format("<%s> %s   %d",item.id, title, item.count));
                dlg.setTitle(R.string.confirm_delete);
                dlg.setCancelable(true);
                dlg.setPositiveButton(R.string.confirm_text, (dialog, which) -> {
                    ItemContent.ITEMS.remove(position);
                    ItemContent.writeToFile(mContext);
                    adapter.notifyItemRemoved(position);
                });
                dlg.setNegativeButton(R.string.cancel_text, null);
                dlg.setOnDismissListener((dialog1)->adapter.notifyItemChanged(position));
                dlg.create().show();
/*
                if (viewModel.getCount() == 0)
                {
                    String msg = String.format("前台計數為 0, 記錄<%s>無法互換!", item.id);
                    Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
                    Vibrator vibrator= (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(350, 200));
                    adapter.notifyItemChanged(position);
                    return;
                }
                CountItem item1 = new CountItem(item.id, viewModel.getTitle(),
                                    viewModel.getCount(), viewModel.getMark());
                viewModel.setTitle(item.content);
                viewModel.setCount(item.count);
                viewModel.setMark(item.mark);
                binding.textViewCount.setText(viewModel.getCountString());
                binding.editTextTitle.setText(viewModel.getTitle());
                ItemContent.ITEMS.set(position, item1);
                adapter.notifyItemChanged(position);
                viewModel.writeConfig(mContext);
                viewModel.writeCountToFile(mContext);
                ItemContent.writeToFile(mContext);
                String msg = String.format("編號<%s>己和前台互換!", item.id);
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
 */
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    public void attachRecyclerViewItemLongClick(MyItemRecyclerViewAdapter adapter) {
        adapter.setOnRecyclerViewItemLongClickListener((position, itemView) -> {
            CountItem countItem = ItemContent.ITEMS.get(position);
            Drawable background = itemView.getBackground();
            itemView.setBackgroundColor(Color.LTGRAY);
            PopupMenu popupMenu = new PopupMenu(mContext, itemView);
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.popup_copy_title) {
                    viewModel.setTitle(countItem.content);
                    binding.editTextTitle.setText(viewModel.getTitle(mContext));
                    viewModel.writeConfig(mContext);
                    return true;
                } else if (item.getItemId() == R.id.popup_bring_front) {
                    if (viewModel.getCount() != 0)
                    {
                        SnackbarWarning(binding.getRoot(), getString(R.string.msg_count_number_not_zero), true);
                        return false;
                    }
                    viewModel.setTitle(countItem.content);
                    viewModel.setCount(countItem.count);
                    viewModel.setMark(countItem.mark);
                    binding.editTextTitle.setText(viewModel.getTitle(mContext));
                    binding.textViewCount.setText(viewModel.getCountString());
                    viewModel.writeConfig(mContext);
                    viewModel.writeCountToFile(mContext);
                    ItemContent.ITEMS.remove(position);
                    ItemContent.writeToFile(mContext);
                    adapter.notifyItemRemoved(position);
                    return true;
                }
                return false;
            });
            popupMenu.setOnDismissListener(menu -> itemView.setBackground(background));
            Vibrator vibrator = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
            popupMenu.show();
            vibrator.vibrate(VibrationEffect.createOneShot(150, 200));
        });
    }



}