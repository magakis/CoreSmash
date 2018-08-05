package com.coresmash;

import com.badlogic.gdx.Gdx;

public class PersistentLong {
    private long value;
    private String prefsName;
    private String prefKey;


    public PersistentLong(String prefKey, String prefsName) {
        this(prefKey, prefsName, 0);
    }

    public PersistentLong(String prefKey, String prefsName, long defValue) {
        value = Gdx.app.getPreferences(prefsName).getLong(prefKey, defValue);
        this.prefKey = prefKey;
        this.prefsName = prefsName;
    }

    public void addAmount(long amount) {
        value += amount;
        setValue(value);
    }

    public void subAmount(long amount) {
        value -= amount;
        setValue(value);
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
        Gdx.app.getPreferences(prefsName).putLong(prefKey, value).flush();
    }

}