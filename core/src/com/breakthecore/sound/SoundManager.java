package com.breakthecore.sound;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;

public class SoundManager {
    private static SoundManager instance = new SoundManager();
    private ObjectMap<String, SoundAsset> sounds;

    public static SoundManager get() {
        return instance;
    }

    private SoundManager() {
        sounds = new ObjectMap<>();
    }

    public void loadSound(String assetName, Sound sound) {
        sounds.put(assetName, new SoundAsset(assetName, sound));
    }

    public SoundAsset getSoundAsset(String assetName) {
        if (!sounds.containsKey(assetName))
            throw new RuntimeException("Sound not loaded: " + assetName);
        return sounds.get(assetName);
    }

    public static class SoundAsset {
        private Sound sound;
        private String soundName;

        private SoundAsset(String name, Sound sound) {
            soundName = name;
            this.sound = sound;
        }

        public long play() {
            return sound.play();
        }

        public long loop() {
            return sound.loop();
        }

        public void stop(int id) {
            sound.stop(id);
        }

        public String getSoundName() {
            return soundName;
        }


    }
}