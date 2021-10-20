package com.tomorrow_eyes.buddhacount;

import android.content.Context;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

public class ItemContent {

    public static final List<CountItem> ITEMS = new ArrayList<>();
    private static final Random random = new Random();

    /**
     * A map of sample (placeholder) items, by ID.
     */
    //public static final Map<String, CountItem> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 9;

    static {
        initList();
    }

    public static void initList() {
        ITEMS.clear();
        addItem(new CountItem("-", "記錄外總計：", 0, LocalDate.now()));
    }

    public static void makePseudoList() {
        // Add some sample items.
        LocalDateTime temp = LocalDateTime.now();
        int count = 0;
        LocalDate localDate;
        for (int i = 1; i <= COUNT; i++) {
            count = random.nextInt(100000);
            temp=temp.minus(count+random.nextInt(1000000), ChronoUnit.SECONDS);
            localDate = LocalDate.of(temp.getYear(), temp.getMonth(), temp.getDayOfMonth());
            addItem(new CountItem(Integer.toString(i),"南無地藏菩薩", count, localDate));
        }
        count = random.nextInt(1000000);
        temp=temp.minus(count+random.nextInt(1000000), ChronoUnit.SECONDS);
        localDate = LocalDate.of(temp.getYear(), temp.getMonth(), temp.getDayOfMonth());
        addItem(new CountItem("-", "記錄外總計：", count, localDate));
    }

    public static void addItem(CountItem item) {
        ITEMS.add(item);
        // ITEM_MAP.put(item.id, item);
    }

    public static void insertItemUpdateList(CountItem item) {
        int len = ITEMS.size();
        if (len > COUNT) {   // 己超長,合併最後記錄
            CountItem item2 = ITEMS.get(len - 2);
            CountItem item1 = ITEMS.get(len - 1);
            ITEMS.remove(len - 1);
            ITEMS.remove(len - 2);
            addItem(new CountItem(item1.id, "記錄外總計：",
                    item1.count+ item2.count, item2.mark));
        }
        ITEMS.add(0, item);
    }

    public static void writeToFile(Context context) {
        String fileName = context.getExternalFilesDir(null) + "/" +
                context.getString(R.string.filename_list);
        FileOutputStream stream;
        try {
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< ITEMS.size();i++) {
                CountItem ci= ITEMS.get(i);
                sb.append(String.format("'%s', %d, %tF%n", ci.content, ci.count, ci.mark));
            }
            byte[] buf = sb.toString().getBytes(StandardCharsets.UTF_8);
            stream = new FileOutputStream(fileName, false);
            stream.write(buf);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addItem(int id, String line) {
        System.out.println(line);
        try {
            String[] split = line.split(",");
            String idStr = Integer.toString(id);
            String content = split[0].substring(1, split[0].length() - 1);
            int count = Integer.parseInt(split[1].trim());
            LocalDate mark = LocalDate.parse(split[2].trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            CountItem item = new CountItem(idStr, content, count, mark);
            ITEMS.add(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void readFromFile(Context context) {
        String fileName = context.getExternalFilesDir(null) + "/" +
                context.getString(R.string.filename_list);
        try {
            FileInputStream stream = new FileInputStream(fileName);
            InputStreamReader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(in);
            String line;
            ITEMS.clear();
            for(int i=1;(line = reader.readLine()) != null; i++) {
                addItem(i, line);
            }
            if (ITEMS.size() == 0) initList();
            ITEMS.get(ITEMS.size()-1).setId("-");  // 最後統計不想是10
            in.close();
            stream.close();
         } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static class CountItem {
        public String id;
        public final String content;
        public final int count;
        public final LocalDate mark;

        public CountItem(String id, String content, int count, LocalDate mark) {
            this.id = id;
            this.content = content;
            this.count = count;
            this.mark = mark;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        @Override
        public String toString() {
            return content;
        }



    }
}
