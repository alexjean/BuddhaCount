package com.tomorrow_eyes.buddhacount;

import static androidx.recyclerview.widget.ItemTouchHelper.*;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.tomorrow_eyes.buddhacount.ItemContent.CountItem;
import com.tomorrow_eyes.buddhacount.databinding.FragmentRecordBinding;

import java.time.LocalDate;
import java.util.List;

public class RecordFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private FragmentRecordBinding binding;
    private MyViewModel viewModel;

    public RecordFragment() {
        // Required empty public constructor
    }

    public static RecordFragment newInstance(String param1, String param2) {
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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_record, container, false);
        binding = FragmentRecordBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(getActivity()).get(MyViewModel.class);
        return binding.getRoot();
    }


    public void areYouOk(DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(getContext());
        dlgAlert.setMessage("確定要重置計數嗎？");
        dlgAlert.setTitle("記錄後重置");
        dlgAlert.setCancelable(true);
        dlgAlert.setPositiveButton("OK", onClickListener);
        dlgAlert.create().show();
    }

    private Context mContext;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.textViewCount.setText(viewModel.getCountString());
        binding.editTextTitle.setText(viewModel.getTitle());
        ItemContent.readFromFile(mContext=getContext());

        binding.buttonReset.setOnClickListener(view1 -> {
            String content = binding.editTextTitle.getText().toString();
            content=content.replace(",","");  // 不准有逗号
            content=content.replace("\n", " ").trim();
            binding.editTextTitle.setText(content);
            String msg = "計數為0, 不入上方列表";
            if (!content.equals(viewModel.getTitle())) {
                msg = "抬頭設為 "+content;
                viewModel.setTitle(content);
                viewModel.writeConfig(mContext);
            }
            if (viewModel.getCount() == 0) {
                Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
                View view2 = snackbar.getView();
                TextView tv = view2.findViewById(com.google.android.material.R.id.snackbar_text);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                snackbar.show();
            }
            else
                areYouOk((dialog, which) -> recordCountAdjustStatistic());
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        attachItemTouchHelper();  // 在onViewCreated呼叫,recyclerView還是null
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
        RecyclerView recyclerView = (RecyclerView) fragment.getView();
        return recyclerView;
    }

    public void recordCountAdjustStatistic() {
        String content = viewModel.getTitle();
        ItemContent.insertItemUpdateList(new CountItem("0", content,
                viewModel.getCount(), LocalDate.now()));
        viewModel.setCount(0);
        binding.textViewCount.setText(viewModel.getCountString());
        viewModel.writeCountToFile(mContext);
        RecyclerView recyclerView = getSubRecyclerView();
        if (recyclerView == null) return;
        //recyclerView.setAdapter(new MyItemRecyclerViewAdapter(ItemContent.ITEMS));
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter != null) adapter.notifyDataSetChanged();
        ItemContent.writeToFile(mContext);
    }

    public void attachItemTouchHelper()
    {
        RecyclerView recyclerView = getSubRecyclerView();
        if (recyclerView == null) return;
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null) return;
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
                    final int swipeFlags = START;
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
                String content = viewModel.getTitle();
                int count = viewModel.getCount();
                LocalDate mark = viewModel.getMark();
                CountItem item1 = new CountItem(item.id, content, count, mark);
                viewModel.setTitle(item.content);
                viewModel.setCount(item.count);
                viewModel.setMark(item.mark);
                binding.textViewCount.setText(viewModel.getCountString());
                binding.editTextTitle.setText(viewModel.getTitle());
                ItemContent.ITEMS.set(position, item1);
                adapter.notifyItemChanged(position);
                Context context = getContext();
                viewModel.writeConfig(context);
                viewModel.writeCountToFile(context);
                ItemContent.writeToFile(context);
                String msg = String.format("編號<%s>己和前台互換!", item.id);
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }


}