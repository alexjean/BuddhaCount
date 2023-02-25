package com.tomorrow_eyes.buddhacount;

import static androidx.recyclerview.widget.ItemTouchHelper.Callback;
import static androidx.recyclerview.widget.ItemTouchHelper.DOWN;
import static androidx.recyclerview.widget.ItemTouchHelper.END;
import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;
import static androidx.recyclerview.widget.ItemTouchHelper.START;
import static androidx.recyclerview.widget.ItemTouchHelper.UP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.tomorrow_eyes.buddhacount.ItemContent.CountItem;
import com.tomorrow_eyes.buddhacount.databinding.FragmentRecordBinding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

public class RecordFragment extends Fragment {

    private FragmentRecordBinding binding;
    private MyViewModel viewModel;

    public RecordFragment() {
        // Required empty public constructor
    }


    public static RecordFragment newInstance() {
        RecordFragment fragment = new RecordFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setHasOptionsMenu(true);  // deprecated
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_record, container, false);
        binding = FragmentRecordBinding.inflate(inflater, container, false);
        ViewModelStoreOwner storeOwner = getActivity();
        if (storeOwner != null) {
            viewModel = new ViewModelProvider(storeOwner).get(MyViewModel.class);
        }
        return binding.getRoot();
    }

    public void SnackbarWarning(View view, String msg , boolean warning) {
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
            View view2 = snackbar.getView();
            TextView tv = view2.findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            snackbar.show();
        }
        if (warning) {
            Vibrator vibrator = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(350, 200));
        }
    }

    private void areYouOk(int gravity, String msg, DialogInterface.OnClickListener onClickListener) {
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

    private final View.OnClickListener onClickResetSaveListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String msg = getString(R.string.msg_count_is_zero);
            if (binding != null) {
                String content = binding.editTextTitle.getText().toString();
                content = content.replace(",", "");  // 不准有逗号
                content = content.replace("\n", " ").trim();
                binding.editTextTitle.setText(content);
                if (!content.equals(viewModel.getTitle())) {
                    msg = getString(R.string.msg_settting_title) + " " + content;
                    viewModel.setTitle(content);
                    viewModel.writeConfig(mContext);
                }
            }
            if (viewModel.getCount() == 0)
                SnackbarWarning(view, msg, true);
            else
                areYouOk(Gravity.CENTER, getString(R.string.msg_sure_to_record_than_reset), (dlg, which) -> {
                    if (ItemContent.sizeOver()) {
                        RecyclerView recyclerView = getSubRecyclerView();
                        if (recyclerView != null)
                            recyclerView.scrollToPosition(ItemContent.ITEMS.size() - 1);
                        areYouOk(Gravity.BOTTOM, getString(R.string.msg_list_too_long_will_merge), (dlg1, which1) -> {
                            recordCountAdjustStatistic();
                            recyclerView.scrollToPosition(0);
                        });
                    } else
                        recordCountAdjustStatistic();
                });
        }
    };

    private Context mContext;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getContext();
        if (mContext!=null)
            ItemContent.readFromFile(mContext);
        if (binding!=null) {
            binding.textViewCount.setText(viewModel.getCountString());
            binding.editTextTitle.setText(viewModel.getTitle());
            binding.buttonReset.setOnClickListener(onClickResetSaveListener);
        }
        setupMenu();
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

    private void adapterNotifyDataSetChanged()
    {
        RecyclerView recyclerView = getSubRecyclerView();
        if (recyclerView == null) return;
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public void recordCountAdjustStatistic() {
        String content = viewModel.getTitle();
        ItemContent.insertItemUpdateList(new CountItem("0", content,
                viewModel.getCount(), viewModel.getMark()));
        viewModel.setCount(0);
        viewModel.setMark(LocalDate.now());
        binding.textViewCount.setText(viewModel.getCountString());
        viewModel.writeCountToFile(mContext);
        adapterNotifyDataSetChanged();
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
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAbsoluteAdapterPosition();
                CountItem item = ItemContent.ITEMS.get(position);
                AlertDialog.Builder dlg  = new AlertDialog.Builder(mContext);
                String title = item.getContent();
                if (title.length() > 7) title= title.substring(0, 7)+"..";
                dlg.setMessage(String.format("<%s> %s   %d", item.getId(), title, item.getCount()));
                dlg.setTitle(R.string.confirm_delete);
                dlg.setCancelable(true);
                dlg.setPositiveButton(R.string.confirm_text, (dialog, which) -> {
                    ItemContent.ITEMS.remove(position);
                    ItemContent.writeToFile(mContext);
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

    public void attachRecyclerViewItemLongClick(MyItemRecyclerViewAdapter adapter) {
        adapter.setOnRecyclerViewItemLongClickListener((position, itemView) -> {
            CountItem countItem = ItemContent.ITEMS.get(position);
            Drawable background = itemView.getBackground();
            itemView.setBackgroundColor(Color.LTGRAY);
            PopupMenu popupMenu = new PopupMenu(mContext, itemView);
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.popup_copy_title) {
                    if (viewModel == null) return false;
                    viewModel.setTitle(countItem.getContent());
                    if (binding != null)
                        binding.editTextTitle.setText(viewModel.getTitle());
                    viewModel.writeConfig(mContext);
                    return true;
                } else if (item.getItemId() == R.id.popup_bring_front) {
                    if (viewModel == null) return false;
                    if (viewModel.getCount() != 0)
                    {
                        SnackbarWarning(binding.getRoot(), getString(R.string.msg_count_number_not_zero), true);
                        return false;
                    }
                    viewModel.setTitle(countItem.getContent());
                    viewModel.setCount(countItem.getCount());
                    viewModel.setMark(countItem.getMark());
                    binding.editTextTitle.setText(viewModel.getTitle());
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

    public void setupMenu() {
        MenuProvider provider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu);
                for (int i = 0; i< menu.size(); i++)
                    menu.getItem(i).setVisible(false);
                menu.findItem(R.id.backup_to_disk).setVisible(true);
                menu.findItem(R.id.restore_backup).setVisible(true);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.backup_to_disk) {
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("text/plain");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    mBackupForResult.launch(intent);
                } else if (id == R.id.restore_backup) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("text/plain");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    mRestoreForResult.launch(intent);
                } else return false;
                return true;
            }
        };
        MenuHost host = (MenuHost) requireActivity();
        host.addMenuProvider(provider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    ActivityResultLauncher<Intent> mBackupForResult =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent == null) return;
                    Uri uri = intent.getData();
                    if (uri == null) return;
                    try {
                        ContentResolver resolver = mContext.getContentResolver();
                        // 用Intent.ACTION_CREATE_DOCUMENT不會同名，會加(1)
                        // write truncate需要嗎? 但確實發現覆寫，結束後面還有
                        OutputStream os = resolver.openOutputStream(uri, "wt"); // write truncate,
                        if( os != null ) {
                            os.write(ItemContent.getBytes());
                            os.close();
                        }
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                }
        });
    ActivityResultLauncher<Intent> mRestoreForResult =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent intent = result.getData();
                if (intent == null) return;
                Uri uri = intent.getData();
                if (uri == null) return;
                try {
                    InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
                    if( inputStream == null ) {
                        SnackbarWarning(binding.getRoot(), "找不到指定檔案!", true);
                        return;
                    }
                    boolean b = ItemContent.streamToItems(inputStream);
                    inputStream.close();
                    adapterNotifyDataSetChanged();

                    AlertDialog.Builder builder  = new AlertDialog.Builder(mContext);
                    String msg=b?"":getString(R.string.reading_error_msg)+"\r\n";
                    builder.setMessage(msg + getString(R.string.backup_override_confirm));
                    builder.setTitle(R.string.restore_backup);
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.confirm_text, (dlg, wh)->ItemContent.writeToFile(mContext));
                    builder.setNegativeButton(R.string.cancel_text, (dlg, wh)->{
                        ItemContent.readFromFile(mContext);
                        adapterNotifyDataSetChanged();
                    });
                    builder.setOnDismissListener((dlg)->{
                        ItemContent.readFromFile(mContext);
                        adapterNotifyDataSetChanged();
                    });
                    AlertDialog dialog = builder.create();
                    dialog.getWindow().setGravity(Gravity.BOTTOM);
                    dialog.show();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });


}