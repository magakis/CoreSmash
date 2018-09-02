package com.archapp.coresmash;

import com.badlogic.gdx.Gdx;

public class PersistentBoolean {
    private boolean value;
    private String prefsName;
    private String prefKey;


    public PersistentBoolean(String prefKey, String prefsName) {
        this(prefKey, prefsName, false);
    }

    public PersistentBoolean(String prefKey, String prefsName, boolean defValue) {
        value = Gdx.app.getPreferences(prefsName).getBoolean(prefKey, defValue);
        this.prefKey = prefKey;
        this.prefsName = prefsName;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
        Gdx.app.getPreferences(prefsName).putBoolean(prefKey, value).flush();
    }
}