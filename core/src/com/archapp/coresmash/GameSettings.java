package com.archapp.coresmash;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class GameSettings {
    private static final GameSettings instance = new GameSettings();
    private static final String PREFS_NAME = "user_settings";

    public static final String MENU_MUSIC_ENABLED = "menu_music_enabled";
    public static final String GAME_MUSIC_ENABLED = "game_music_enabled";
    public static final String SOUND_EFFECTS_ENABLED = "sound_effects_enabled";

    private PropertyChangeSupport listeners;

    private PersistentBoolean menuMusicEnabled;
    private PersistentBoolean gameMusicEnabled;
    private PersistentBoolean soundEffectsEnabled;

    private GameSettings() {
        listeners = new PropertyChangeSupport(this);

        menuMusicEnabled = new PersistentBoolean(MENU_MUSIC_ENABLED, PREFS_NAME, true);
        gameMusicEnabled = new PersistentBoolean(GAME_MUSIC_ENABLED, PREFS_NAME, true);
        soundEffectsEnabled = new PersistentBoolean(SOUND_EFFECTS_ENABLED, PREFS_NAME, true);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(listener);
    }

    public static GameSettings get() {
        return instance;
    }

    public boolean isMenuMusicEnabled() {
        return menuMusicEnabled.getValue();
    }

    public void setMenuMusicEnabled(boolean menuMusicEnabled) {
        boolean oldValue = this.menuMusicEnabled.getValue();
        this.menuMusicEnabled.setValue(menuMusicEnabled);
        listeners.firePropertyChange(MENU_MUSIC_ENABLED, oldValue, menuMusicEnabled);
    }

    public boolean isGameMusicEnabled() {
        return gameMusicEnabled.getValue();
    }

    public void setGameMusicEnabled(boolean gameMusicEnabled) {
        boolean oldValue = this.gameMusicEnabled.getValue();
        this.gameMusicEnabled.setValue(gameMusicEnabled);
        listeners.firePropertyChange(GAME_MUSIC_ENABLED, oldValue, gameMusicEnabled);
    }

    public boolean isSoundEffectsEnabled() {
        return soundEffectsEnabled.getValue();
    }

    public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
        boolean oldValue = this.soundEffectsEnabled.getValue();
        this.soundEffectsEnabled.setValue(soundEffectsEnabled);
        listeners.firePropertyChange(SOUND_EFFECTS_ENABLED, oldValue, soundEffectsEnabled);
    }
}
