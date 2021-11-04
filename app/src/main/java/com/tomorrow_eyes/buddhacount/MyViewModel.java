package com.tomorrow_eyes.buddhacount;
import android.content.Context;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MyViewModel extends ViewModel {
    private MutableLiveData<Integer> count;
    private MutableLiveData<LocalDate> mark;
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

    public LocalDate getMark() {
        if (mark == null) {
            mark = new MediatorLiveData<>();
            mark.setValue(LocalDate.now());
        }
        return mark.getValue();
    }

    public void setMark(LocalDate value) {
        if (mark == null) mark = new MediatorLiveData<>();
        mark.setValue(value);
    }


    public Boolean getWoodenKnocker() {
        if (woodenKnocker != null) return woodenKnocker.getValue();
        woodenKnocker = new MutableLiveData<>();
        woodenKnocker.setValue(true);
        return true;
    }

    public void setWoodenKnocker(Boolean value) {
        if (woodenKnocker == null) woodenKnocker = new MutableLiveData<>();
        woodenKnocker.setValue(value);
    }

    public void writeCountToFile(Context context) {
        String fileName = context.getExternalFilesDir(null) + "/" +
                context.getString(R.string.filename_count);
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd");
            String str = getCountString() + "," + getMark().format(formatter);
            byte[] buf = str.getBytes(StandardCharsets.UTF_8);
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
        int count1 = 0;
        LocalDate date1 = LocalDate.now();
        try {
            FileInputStream stream = new FileInputStream(fileName);
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(reader,256);
            String line = bufferedReader.readLine();
            try {
                String[] split = line.split(",");
                count1 = Integer.parseInt(split[0].trim());
                if (split.length > 1) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd");
                    date1 = LocalDate.parse(split[1].trim(), formatter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setCount(count1);
        setMark(date1);
     }

    public String getTitle() {
        if (title == null) {
            title = new MediatorLiveData<>();
            title.setValue("南無阿隬陀佛");
        }
        return title.getValue();
    }

    public void setTitle(String value) {
        if (title == null) title = new MutableLiveData<>();
        title.setValue(value);
    }

    public void readConfig(Context context) {
        String fileName = context.getExternalFilesDir(null) + "/" +
                context.getString(R.string.filename_config);
        try {
            byte[] buf = new byte[4096];
            FileInputStream stream = new FileInputStream(fileName);
            int i = stream.read(buf, 0, 4095);
            stream.close();
            if (i > 0) {
                String strJson = new String(buf, 0, i, StandardCharsets.UTF_8);
                JSONObject jsonObj = new JSONObject(strJson);
                setTitle(jsonObj.getString("Title"));
                setWoodenKnocker(jsonObj.getBoolean("SoundSwitch"));
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
            jsonObject.put("SoundSwitch", getWoodenKnocker());

            byte[] buf = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
            FileOutputStream stream = new FileOutputStream(fileName, false);
            stream.write(buf);
            stream.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

}
