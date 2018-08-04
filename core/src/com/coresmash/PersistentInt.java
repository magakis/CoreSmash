package com.coresmash;

import com.badlogic.gdx.Gdx;

public class PersistentInt {
    private int value;
    private String prefsName;
    private String prefKey;


    public PersistentInt(String prefKey, String prefsName) {
        this(prefKey, prefsName, 0);
    }

    public PersistentInt(String prefKey, String prefsName, int defValue) {
        value = Gdx.app.getPreferences(prefsName).getInteger(prefKey, defValue);
        this.prefKey = prefKey;
        this.prefsName = prefsName;
    }

    public void addAmount(int amount) {
        value += amount;
        setValue(value);
    }

    public void subAmount(int amount) {
        value -= amount;
        setValue(value);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        Gdx.app.getPreferences(prefsName).putInteger(prefKey, value).flush();
    }

}