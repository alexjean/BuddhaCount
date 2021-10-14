package com.tomorrow_eyes.buddhacount;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ItemContent {

    public static final List<CountItem> ITEMS = new ArrayList<>();
    private static final Random random = new Random();

    /**
     * A map of sample (placeholder) items, by ID.
     */
    public static final Map<String, CountItem> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 9;

    static {
        // Add some sample items.
        LocalDateTime temp = LocalDateTime.now();
        int count;
        for (int i = 1; i <= COUNT; i++) {
            count = random.nextInt(100000);
            temp=temp.minus(count+random.nextInt(1000000), ChronoUnit.SECONDS);
            addItem(new CountItem(Integer.toString(i),"南無地藏菩薩", count, temp ));
        }
        count = random.nextInt(1000000);
        temp=temp.minus(count+random.nextInt(1000000), ChronoUnit.SECONDS);
        addItem(new CountItem("---", "記錄外總計:", count, temp));
    }

    private static void addItem(CountItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static class CountItem {
        public final String id;
        public final String content;
        public final int count;
        public final LocalDateTime mark;

        public CountItem(String id, String content, int count, LocalDateTime mark) {
            this.id = id;
            this.content = content;
            this.count = count;
            this.mark = mark;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
