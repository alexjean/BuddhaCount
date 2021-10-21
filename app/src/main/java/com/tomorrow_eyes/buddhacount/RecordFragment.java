package com.tomorrow_eyes.buddhacount;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.tomorrow_eyes.buddhacount.databinding.FragmentRecordBinding;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
            content=content.replace(",","").trim();  // 不准有逗号
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

    public void recordCountAdjustStatistic() {
        String content = viewModel.getTitle();
        ItemContent.insertItemUpdateList(new ItemContent.CountItem("0", content,
                viewModel.getCount(), LocalDate.now()));
        viewModel.setCount(0);
        binding.textViewCount.setText(viewModel.getCountString());
        viewModel.writeCountToFile(mContext);
        // FragmentContainerView containerView = binding.fragmentContainerView;
        // not support getFragment() until Fragment:1.4.0
        //ItemFragment itemFragment = (ItemFragment)containerView.getFragment();
        FragmentManager fragmentManager = getChildFragmentManager();
        List<Fragment> list = fragmentManager.getFragments();
        if (list.isEmpty()) return;
        Fragment fragment = list.get(0);
        if (!(fragment instanceof ItemFragment)) return;
        RecyclerView recyclerView = (RecyclerView) fragment.getView();
        assert recyclerView != null;
        recyclerView.setAdapter(new MyItemRecyclerViewAdapter(ItemContent.ITEMS));
        ItemContent.writeToFile(mContext);
    }

}