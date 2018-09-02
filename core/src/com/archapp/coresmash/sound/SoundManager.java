package com.archapp.coresmash.sound;

import com.archapp.coresmash.GameSettings;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SoundManager {
    private static SoundManager instance = new SoundManager();
    private ObjectMap<SoundTrack, SoundEffect> soundList;
    private ObjectMap<MusicTrack, MusicAsset> musicList;
    private List<SoundEffect> playlist;

    private static final float BACKGROUND_MUSIC_VOLUME = 0.5f;

    private boolean inGame;
    private MusicAsset menuMusic, gameMusic;

    public enum MusicTrack {
        MENU_MUSIC, GAME_MUSIC
    }

    public enum SoundTrack {
        BUTTON_CLICK,
        REGULAR_BALL_DESTROY,
        FIREBALL_EXPLOSION,
        FIREBALL_LAUNCH,
        ASTRONAUT_RELEASE
    }

    public static SoundManager get() {
        return instance;
    }

    private SoundManager() {
        soundList = new ObjectMap<>();
        musicList = new ObjectMap<>();
        playlist = new ArrayList<>();

        GameSettings.get().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                boolean newValue = (boolean) event.getNewValue();

                if (event.getPropertyName().equals(GameSettings.GAME_MUSIC_ENABLED)) {
                    if (inGame) {
                        if (newValue)
                            gameMusic.play();
                        else
                            gameMusic.pause();
                    }
                } else if (event.getPropertyName().equals(GameSettings.MENU_MUSIC_ENABLED)) {
                    if (!inGame) {
                        if (newValue)
                            menuMusic.play();
                        else
                            menuMusic.pause();
                    }
                }
            }
        });
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

    public void play(SoundTrack track) {
        getSoundAsset(track).play();
    }


    public void playGameMusic() {
        inGame = true;
        menuMusic.pause();
        if (GameSettings.get().isGameMusicEnabled()) {
            if (!gameMusic.sound.isPlaying()) {
                gameMusic.play();
            }
        }
    }

    public void playMenuMusic() {
        inGame = false;
        gameMusic.stop();
        if (GameSettings.get().isMenuMusicEnabled()) {
            if (!menuMusic.sound.isPlaying()) {
                menuMusic.play();
            }
        }
    }

    public void setMenuMusic(MusicTrack track) {
        boolean playing = menuMusic != null && menuMusic.sound.isPlaying();

        menuMusic = getMusicAsset(track);
        menuMusic.setLooping(true);
        menuMusic.setVolume(BACKGROUND_MUSIC_VOLUME);
        if (playing)
            menuMusic.play();
    }

    public void setGameMusic(MusicTrack track) {
        boolean playing = gameMusic != null && gameMusic.sound.isPlaying();

        gameMusic = getMusicAsset(track);
        gameMusic.setLooping(true);
        gameMusic.setVolume(BACKGROUND_MUSIC_VOLUME);
        if (playing)
            gameMusic.play();
    }

    public void loadSound(SoundTrack track, Sound sound) {
        loadSound(track, sound, 1);
    }

    public void loadSound(SoundTrack track, Sound sound, float volume) {
        soundList.put(track, new SoundEffect(sound, volume));
    }

    public void loadMusic(MusicTrack track, Music sound) {
        musicList.put(track, new MusicAsset(sound));
    }

    private SoundEffect getSoundAsset(SoundTrack track) {
        if (!soundList.containsKey(track))
            throw new RuntimeException("Sound not loaded: " + track);
        return soundList.get(track);
    }

    private MusicAsset getMusicAsset(MusicTrack track) {
        if (!musicList.containsKey(track))
            throw new RuntimeException("Music not loaded: " + track);
        return musicList.get(track);
    }

    public class SoundEffect {
        private Sound sound;
        private float volume;
        private int playCount;

        private SoundEffect(Sound sound) {
            this(sound, 1);
        }

        private SoundEffect(Sound sound, float volume) {
            this.sound = sound;
            this.volume = volume;
        }

        private void playSound() {
            if (GameSettings.get().isSoundEffectsEnabled())
                sound.play(volume);
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

        private MusicAsset(Music sound) {
            this.sound = sound;
        }

        public void play() {
            sound.play();
        }

        public void pause() {
            sound.pause();
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
    }
}