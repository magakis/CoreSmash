package com.coresmash;

import com.badlogic.gdx.Gdx;

public class PersistentString {
    private String value;
    private String prefsName;
    private String prefKey;


    public PersistentString(String prefsName, String prefKey) {
        this(prefKey, prefsName, "");
    }

    public PersistentString(String prefsName, String prefKey, String defValue) {
        value = Gdx.app.getPreferences(prefsName).getString(prefKey, defValue);
        this.prefKey = prefKey;
        this.prefsName = prefsName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        Gdx.app.getPreferences(prefsName).putString(prefKey, value).flush();
    }

}