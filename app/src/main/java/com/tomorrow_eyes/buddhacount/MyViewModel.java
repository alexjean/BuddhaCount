package com.tomorrow_eyes.buddhacount;
import android.content.Context;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;

public class MyViewModel extends ViewModel {
    private MutableLiveData<Integer> count;
    private MutableLiveData<Boolean> woodenKnocker;
    private MutableLiveData<String>  title;


    public int getCount() {
        if (count != null) return count.getValue();
        count = new MutableLiveData<>();
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
        if (count == null) count = new MutableLiveData<>();
        count.setValue(value);
    }

    public Boolean getWoodenKnocker() {
        if (woodenKnocker != null) return woodenKnocker.getValue();
        woodenKnocker = new MutableLiveData<>();
        woodenKnocker.setValue(false);
        return false;
    }

    public void setWoodenKnocker(Boolean value) {
        if (woodenKnocker == null) woodenKnocker = new MutableLiveData<>();
        woodenKnocker.setValue(value);
    }

    public void writeCountToFile(Context context) {
        String fileName = context.getExternalFilesDir(null) + "/" +
                context.getString(R.string.filename_count);
        try {
            byte[] buf = getCountString().getBytes(StandardCharsets.UTF_8);
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
                String str = new String(buf, 0, i, StandardCharsets.UTF_8);
                i = Integer.parseInt(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        setCount(i);
     }

    public String getTitle() {
        if (title != null) return title.getValue();
        title = new MediatorLiveData<>();
        title.setValue("南無阿隬陀佛");
        return "南無阿隬陀佛";
    }

    public void setTitle(String value) {
        if (title == null) title = new MutableLiveData<>();
        title.setValue(value);
    }

    public void readConfig(Context context) {
        String fileName = context.getExternalFilesDir(null) + "/" +
                context.getString(R.string.filename_config);
        try {
            byte[] buf = new byte[256];
            FileInputStream stream = new FileInputStream(fileName);
            int i = stream.read(buf, 0, 250);
            stream.close();
            if (i > 0) {
                String strJson = new String(buf, 0, i, StandardCharsets.UTF_8);
                JSONObject jsonObj = new JSONObject(strJson);
                setTitle(jsonObj.getString("Title"));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeConfig(Context context) {
        String fileName = context.getExternalFilesDir(null) + "/" +
                context.getString(R.string.filename_config);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Title", getTitle());

            byte[] buf = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
            FileOutputStream stream = new FileOutputStream(fileName, false);
            stream.write(buf);
            stream.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

}
