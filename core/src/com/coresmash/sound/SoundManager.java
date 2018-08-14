package com.coresmash.sound;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;

public class SoundManager {
    private static SoundManager instance = new SoundManager();
    private ObjectMap<String, SoundAsset> soundList;
    private ObjectMap<String, MusicAsset> musicList;

    public static SoundManager get() {
        return instance;
    }

    private SoundManager() {
        soundList = new ObjectMap<>();
        musicList = new ObjectMap<>();
    }

    public void loadSound(String assetName, Sound sound) {
        soundList.put(assetName, new SoundAsset(assetName, sound));
    }

    public void loadMusic(String assetName, Music sound) {
        musicList.put(assetName, new MusicAsset(assetName, sound));
    }

    public SoundAsset getSoundAsset(String assetName) {
        if (!soundList.containsKey(assetName))
            throw new RuntimeException("Sound not loaded: " + assetName);
        return soundList.get(assetName);
    }

    public MusicAsset getMusicAsset(String assetName) {
        if (!musicList.containsKey(assetName))
            throw new RuntimeException("Music not loaded: " + assetName);
        return musicList.get(assetName);
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

        /**
         * 0 : Silent - 1 : Max
         */
        public void setVolume(long id, float volume) {
            sound.setVolume(id, volume);
        }

        public void stop(int id) {
            sound.stop(id);
        }

        public String getSoundName() {
            return soundName;
        }
    }

    public static class MusicAsset {
        private Music sound;
        private String soundName;

        private MusicAsset(String name, Music sound) {
            soundName = name;
            this.sound = sound;
        }

        public void play() {
            sound.play();
        }

        public void setLooping(boolean loop) {
            sound.setLooping(loop);
        }

        /**
         * 0 : Silent - 1 : Max
         */
        public void setVolume(float volume) {
            sound.setVolume(volume);
        }

        public void stop() {
            sound.stop();
        }

        public String getSoundName() {
            return soundName;
        }
    }
}