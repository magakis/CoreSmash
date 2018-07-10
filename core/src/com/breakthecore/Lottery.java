package com.breakthecore;

import com.breakthecore.tiles.PowerupType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Lottery {
    private List<Item> items;
    private Random rand;
    private int totalChances;

    public Lottery() {
        rand = new Random();
        items = new ArrayList<>(10);

        items.add(new Item(null, 0, 10));
        items.add(new Item(PowerupType.FIREBALL, 1, 7));
        items.add(new Item(PowerupType.FIREBALL, 2, 2));
        items.add(new Item(PowerupType.FIREBALL, 3, 1));

        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item item, Item t1) {
                return Integer.compare(item.chance, t1.chance);
            }
        });

        for (Item item : items) {
            totalChances += item.chance;
        }
    }

    public Item draw() {
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

    public static class Item {
        private PowerupType type;
        private int amount;
        private int chance; // 1 - 10 ? 1 = least 10 = most

        Item(PowerupType type, int amount, int chance) {
            this.type = type;
            this.amount = amount;
            this.chance = chance;
        }

        public PowerupType getType() {
            return type;
        }

        public int getAmount() {
            return amount;
        }
    }
}
