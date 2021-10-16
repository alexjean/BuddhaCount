package com.tomorrow_eyes.buddhacount;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.tomorrow_eyes.buddhacount.databinding.FragmentKsitigarbhaBinding;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class KsitigarbhaFragment extends Fragment {

    private FragmentKsitigarbhaBinding binding;
    private MyViewModel viewModel;
    private Color bgColor;
    private MediaPlayer mPlayer;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        binding = FragmentKsitigarbhaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel.ReadCountFromFile(getContext());
        binding.textviewFirst.setText(viewModel.getCountString());
        Resources.Theme theme = getActivity().getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.colorOnPrimary, typedValue, true);
        bgColor = Color.valueOf(typedValue.data);
        mPlayer=MediaPlayer.create(getContext(), R.raw.wooden_knocker);
        try {
            mPlayer.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        binding.buttonCount.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_DOWN) return false;
                if (viewModel.getWoodenKnocker()) {
                    if (mPlayer.isPlaying()) mPlayer.stop();
                    mPlayer.start();
                }
                return false;
            }
        });
        binding.buttonCount.setOnClickListener(view1 -> {
            viewModel.addCount();
            binding.textviewFirst.setText(Integer.toString(viewModel.getCount()));
            float r = bgColor.red();
            float g = bgColor.green();
            float b = bgColor.blue();
            if ((r > 0.1) && (b > 0.1) && (g > 0.1)){
                r -= 0.01; g -= 0.01; b -= 0.01;
                bgColor = Color.valueOf(r, g, b);
                binding.firstFrag.setBackgroundColor(bgColor.toArgb());
            }
            viewModel.writeCountToFile(getContext());;
        });
    }

    @Override
    public void onDestroyView() {
        mPlayer.release();
        super.onDestroyView();
        binding = null;
    }


}