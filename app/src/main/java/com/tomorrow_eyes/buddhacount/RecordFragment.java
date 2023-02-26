package com.tomorrow_eyes.buddhacount;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import androidx.recyclerview.widget.RecyclerView;

import com.tomorrow_eyes.buddhacount.ItemContent.CountItem;
import com.tomorrow_eyes.buddhacount.databinding.FragmentRecordBinding;

import java.time.LocalDate;
import java.util.List;

public class RecordFragment extends Fragment implements MsgUtility,
        SwipeItemTouchHelper, LongClickItemHelper, BackupRestoreCallback {

    private FragmentRecordBinding binding; // Closure內沒有getContext可叫，故存
    private MyViewModel viewModel; // Closure內沒有getContext可叫，故存

    public RecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void snackbarWarning(String msg, boolean warning) {  // for other interface to call
        MsgUtility.super.snackbarWarning(msg,warning);
    }

    public static RecordFragment newInstance() {
        RecordFragment fragment = new RecordFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupMenu();
        final Context _context = getContext();
        if (_context ==null) return;
        if (binding == null) return;
        if (viewModel == null) return;
        ItemContent.readFromFile(_context);
        final FragmentRecordBinding _binding = binding;
        final MyViewModel _viewModel = viewModel;
        _binding.textViewCount.setText(_viewModel.getCountString());
        _binding.editTextTitle.setText(_viewModel.getTitle());
        _binding.buttonReset.setOnClickListener( view1 -> {
            String content = _binding.editTextTitle.getText().toString();
            content = content.replace(",", "");  // 不准有逗号
            content = content.replace("\n", " ").trim();
            _binding.editTextTitle.setText(content);

            String msg = getString(R.string.msg_count_is_zero);
            if (!content.equals(_viewModel.getTitle())) {
                msg = getString(R.string.msg_settting_title) + " " + content;
                _viewModel.setTitle(content);
                _viewModel.writeConfig(_context);
            }
            if (_viewModel.getCount() == 0)
                snackbarWarning(msg, true);
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
            attachRecyclerViewItemLongClick((MyItemRecyclerViewAdapter) adapter, viewModel, binding);
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
        if (!(fragment instanceof ItemListFragment)) return null;
        return (RecyclerView) fragment.getView();
    }

    public void adapterNotifyDataSetChanged()
    {
        RecyclerView recyclerView = getSubRecyclerView();
        if (recyclerView == null) return;
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public void recordCountAdjustStatistic() {
        if (viewModel == null) return;
        final MyViewModel _vm = viewModel;
        String content = _vm.getTitle();
        ItemContent.insertItemUpdateList(new CountItem("0", content,
                _vm.getCount(), _vm.getMark()));
        _vm.setCount(0);
        _vm.setMark(LocalDate.now());
        adapterNotifyDataSetChanged();
        final Context _context = getContext();
        if (_context != null) {
            _vm.writeCountToFile(_context);
            ItemContent.writeToFile(_context);
        }
        if (binding != null) {
            final FragmentRecordBinding _binding = binding;
            _binding.textViewCount.setText(_vm.getCountString());
            snackbarWarning(getString(R.string.msg_already_on_top), false);
        }
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
                                  result -> {  backupCallback(result); }
        );
    ActivityResultLauncher<Intent> mRestoreForResult =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                                  result -> {   restoreCallback(result); }
        );

}