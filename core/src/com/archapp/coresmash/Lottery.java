package com.archapp.coresmash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Lottery<T> {
    private List<Item> items;
    private Random rand;
    private int totalChances;

    /* It also supports null as 'no reward' but it's not used atm */
    public Lottery() {
        rand = new Random();
        items = new ArrayList<>(10);

        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item item, Item t1) {
                return Integer.compare(item.chance, t1.chance);
            }
        });
    }

    public void addPossibleItem(T type, int amount, int chance) {
        items.add(new Item<>(type, amount, chance));
        totalChances += chance;
    }

    public Item<T> draw() {
        int num = rand.nextInt(totalChances);
        int searched = 0;
        for (Item item : items) {
            searched += item.chance;
            if (num < searched) {
                return item;
            }
        }
        throw new RuntimeException("Couldn't find LotteryItem. Num:" + num + " TotalChance:" + totalChances);
    }

    public static class Item<T> {
        private T type;
        private int amount;
        private int chance; // 1 - 10 ? 1 = least 10 = most

        private Item(T type, int amount, int chance) {
            this.type = type;
            this.amount = amount;
            this.chance = chance;
        }

        public T getType() {
            return type;
        }

        public int getAmount() {
            return amount;
        }
    }
}
