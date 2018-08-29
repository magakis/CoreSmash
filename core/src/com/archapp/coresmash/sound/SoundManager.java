package com.archapp.coresmash.sound;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SoundManager {
    private static SoundManager instance = new SoundManager();
    private ObjectMap<String, SoundEffect> soundList;
    private ObjectMap<String, MusicAsset> musicList;
    private List<SoundEffect> playlist;

    public static SoundManager get() {
        return instance;
    }

    private SoundManager() {
        soundList = new ObjectMap<>();
        musicList = new ObjectMap<>();
        playlist = new ArrayList<>();
    }

    public void update(float delta) {
        Iterator<SoundEffect> iter = playlist.iterator();
        while (iter.hasNext()) {
            SoundEffect asset = iter.next();
            asset.playSound();
            --asset.playCount;
            if (asset.playCount == 0) {
                iter.remove();
            }
        }
    }

    public void loadSound(String assetName, Sound sound) {
        soundList.put(assetName, new SoundEffect(assetName, sound));
    }

    public void loadMusic(String assetName, Music sound) {
        musicList.put(assetName, new MusicAsset(assetName, sound));
    }

    public SoundEffect getSoundAsset(String assetName) {
        if (!soundList.containsKey(assetName))
            throw new RuntimeException("Sound not loaded: " + assetName);
        return soundList.get(assetName);
    }

    public MusicAsset getMusicAsset(String assetName) {
        if (!musicList.containsKey(assetName))
            throw new RuntimeException("Music not loaded: " + assetName);
        return musicList.get(assetName);
    }

    public class SoundEffect {
        private Sound sound;
        private String soundName;
        private int playCount;

        private SoundEffect(String name, Sound sound) {
            soundName = name;
            this.sound = sound;
        }

        private void playSound() {
            sound.play();
        }

        public void setVolume(float volume) {
        }

        public void loop(float volume) {
            sound.loop(volume);
        }

        public void loop() {
            sound.loop();
        }

        public void play() {
            if (playCount == 0) {
                playlist.add(this);
                ++playCount;
            }
        }

        public void stop() {
            sound.stop();
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