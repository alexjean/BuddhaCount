package com.tomorrow_eyes.buddhacount;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.tomorrow_eyes.buddhacount.databinding.FragmentKsitigarbhaBinding;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class KsitigarbhaFragment extends Fragment {

    private FragmentKsitigarbhaBinding binding;
    private int count = 0;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentKsitigarbhaBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        count = ReadFromFile();
        binding.textviewFirst.setText(Integer.toString(count));
        binding.buttonCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count++;
                binding.textviewFirst.setText(Integer.toString(count));
                WriteToFile(count);
            }
        });
        /*
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(KsitigarbhaFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
        */
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void WriteToFile(int count) {
        Context context = getContext();
        String fileName = context.getExternalFilesDir(null) + "/" +
                          context.getString(R.string.filename_count);
        FileOutputStream stream;
        try {
            byte[] buf = Integer.toString(count).getBytes();
            stream = new FileOutputStream(fileName, false);
            stream.write(buf);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int ReadFromFile() {
        Context context = getContext();
        String fileName = context.getExternalFilesDir(null) + "/" +
                context.getString(R.string.filename_count);
        FileInputStream stream;
        int i = 0;
        try {
            byte[] buf = new byte[256];
            stream = new FileInputStream(fileName);
            i = stream.read(buf, 0, 250);
            stream.close();
            if (i > 0) {
                String str = new String(buf, 0, i);
                i = Integer.parseInt(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return i;
    }

}