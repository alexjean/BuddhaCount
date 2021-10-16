package com.tomorrow_eyes.buddhacount;
import android.content.Context;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyViewModel extends ViewModel {
    private MutableLiveData<Integer> count;
    private MutableLiveData<Boolean> woodenKnocker;

    public int getCount() {
        if (count != null) return count.getValue();
        count = new MediatorLiveData<>();
        count.setValue(0);
        return 0;
    }

    public String getCountString()
    {
        return Integer.toString(getCount());
    }

    public void addCount() {
        int i = getCount();
        count.setValue(i + 1);
    }

    public void setCount(int value) {
        if (count == null) count = new MediatorLiveData<>();
        count.setValue(value);
    }

    public Boolean getWoodenKnocker() {
        if (woodenKnocker != null) return woodenKnocker.getValue();
        woodenKnocker = new MediatorLiveData<>();
        woodenKnocker.setValue(false);
        return false;
    }

    public void setWoodenKnocker(Boolean value) {
        if (woodenKnocker == null) woodenKnocker = new MediatorLiveData<>();
        woodenKnocker.setValue(value);
    }


    public void writeCountToFile(Context context) {
        String fileName = context.getExternalFilesDir(null) + "/" +
                context.getString(R.string.filename_count);
        try {
            byte[] buf = getCountString().getBytes();
            FileOutputStream stream = new FileOutputStream(fileName, false);
            stream.write(buf);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ReadCountFromFile(Context context) {
        String fileName = context.getExternalFilesDir(null) + "/" +
                context.getString(R.string.filename_count);
        int i = 0;
        try {
            byte[] buf = new byte[256];
            FileInputStream stream = new FileInputStream(fileName);
            i = stream.read(buf, 0, 250);
            stream.close();
            if (i > 0) {
                String str = new String(buf, 0, i);
                i = Integer.parseInt(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        setCount(i);
     }


}
