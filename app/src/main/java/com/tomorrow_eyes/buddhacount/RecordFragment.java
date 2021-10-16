package com.tomorrow_eyes.buddhacount;

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
import android.widget.Toast;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.textViewCount.setText(viewModel.getCountString());
        ItemContent.readFromFile(getContext());

        binding.buttonReset.setOnClickListener(view1 -> {
            if (viewModel.getCount() == 0) {
                Toast.makeText(getContext(),"計數為0, 無從存檔!",Toast.LENGTH_SHORT).show();
                return;
            }
            ItemContent.insertItemUpdateList(new ItemContent.CountItem("0","南無地藏菩薩",
                    viewModel.getCount(), LocalDate.now()));
            viewModel.setCount(0);
            binding.textViewCount.setText(viewModel.getCountString());
            // FragmentContainerView containerView = binding.fragmentContainerView;
            // not support getFragment() until Fragment:1.4.0
            //ItemFragment itemFragment = (ItemFragment)containerView.getFragment();
            FragmentManager fragmentManager = getChildFragmentManager();
            List<Fragment> list = fragmentManager.getFragments();
            if (list == null || list.isEmpty()) return;
            Fragment fragment = list.get(0);
            if (!(fragment instanceof ItemFragment)) return;
            RecyclerView recyclerView = (RecyclerView) fragment.getView();
            recyclerView.setAdapter(new MyItemRecyclerViewAdapter(ItemContent.ITEMS));
            ItemContent.writeToFile(getContext());
        });

    }
}