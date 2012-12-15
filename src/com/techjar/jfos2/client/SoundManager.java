package com.techjar.jfos2.client;

import de.cuina.fireandfuel.CodecJLayerMP3;
import java.io.File;
import java.net.MalformedURLException;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOgg;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

/**
 *
 * @author Techjar
 */
public class SoundManager {
    protected final SoundSystem soundSystem;
    protected final File soundPath;
    protected final File soundPathCustom;
    protected float effectVolume = 1;
    protected float musicVolume = 1;
    protected long nextId;
    
    public SoundManager() {
        soundSystem = new SoundSystem();
        soundPath = new File("resources/sounds/");
        soundPathCustom = new File("resources/sounds/");
    }

    private File getFile(String file) {
        File fil = new File(soundPathCustom, file);
        if (!fil.exists()) fil = new File(soundPath, file);
        return fil;
    }
    
    public void loadSound(String file) {
        try {
            soundSystem.loadSound(getFile(file).toURI().toURL(), file);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void unloadSound(String file) {
        soundSystem.unloadSound(file);
    }

    public boolean isPlaying(String source) {
        return soundSystem.playing(source);
    }

    public String playEffect(String file, boolean loop) {
        String source = playTemporarySound(file, loop);
        if (source != null) soundSystem.setVolume(source, effectVolume);
        return source;
    }

    public String playMusic(String file, boolean loop) {
        String source = playStreamingSound(file, loop);
        if (source != null) soundSystem.setVolume(source, musicVolume);
        return source;
    }

    public String playLoadedMusic(String file, boolean loop) {
        String source = playSound(file, loop);
        if (source != null) soundSystem.setVolume(source, musicVolume);
        return source;
    }
    
    public String playSound(String file, boolean loop) {
        String source = "source_" + nextId++;
        try {
            soundSystem.newSource(false, source, getFile(file).toURI().toURL(), file, loop, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return null;
        }
        soundSystem.play(source);
        return source;
    }
    
    public String playStreamingSound(String file, boolean loop) {
        String source = "source_" + nextId++;
        try {
            soundSystem.newStreamingSource(false, source, getFile(file).toURI().toURL(), file, loop, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return null;
        }
        soundSystem.play(source);
        return source;
    }
    
    public String playTemporarySound(String file, boolean loop) {
        try {
            return soundSystem.quickPlay(false, getFile(file).toURI().toURL(), file, loop, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public String playTemporaryStreamingSound(String file, boolean loop) {
        try {
            return soundSystem.quickStream(false, getFile(file).toURI().toURL(), file, loop, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public void playSound(String source) {
        soundSystem.play(source);
    }
    
    public void pauseSound(String source) {
        soundSystem.pause(source);
    }

    public void stopSound(String source) {
        soundSystem.stop(source);
    }

    public void rewindSound(String source) {
        soundSystem.rewind(source);
    }
    
    public void removeSound(String source) {
        soundSystem.removeSource(source);
    }
    
    public void stopTemporarySounds() {
        soundSystem.removeTemporarySources();
    }
    
    public float getMasterVolume() {
        return soundSystem.getMasterVolume();
    }
    
    public void setMasterVolume(float volume) {
        soundSystem.setMasterVolume(volume);
    }

    public float getEffectVolume() {
        return effectVolume;
    }

    public void setEffectVolume(float effectVolume) {
        this.effectVolume = effectVolume;
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
    }
    
    public float getSoundVolume(String source) {
        return soundSystem.getPitch(source);
    }
    
    public float getSoundPitch(String source) {
        return soundSystem.getPitch(source);
    }
    
    public void setSoundVolume(String source, float volume) {
        soundSystem.setVolume(source, volume);
    }
    
    public void setSoundPitch(String source, float pitch) {
        soundSystem.setVolume(source, pitch);
    }
    
    public SoundSystem getSoundSystem() {
        return soundSystem;
    }
    
    
    static {
        try {
            SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
            SoundSystemConfig.setCodec("mp3", CodecJLayerMP3.class);
            SoundSystemConfig.setCodec("ogg", CodecJOgg.class);
            SoundSystemConfig.setCodec("wav", CodecWav.class);
        }
        catch (SoundSystemException ex) {
            ex.printStackTrace();
        }
    }
}
