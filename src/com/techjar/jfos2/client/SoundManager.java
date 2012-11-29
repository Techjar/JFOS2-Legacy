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
    protected long nextId;
    
    public SoundManager() {
        soundSystem = new SoundSystem();
        soundPath = new File("resources/sounds/");
    }
    
    public void loadSound(String file) {
        try {
            soundSystem.loadSound(new File(soundPath, file).toURI().toURL(), file);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void unloadSound(String file) {
        soundSystem.unloadSound(file);
    }
    
    public String playSound(String file, boolean loop) {
        String source = "source_" + nextId++;
        try {
            soundSystem.newSource(false, source, new File(soundPath, file).toURI().toURL(), file, loop, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
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
            soundSystem.newStreamingSource(false, source, new File(soundPath, file).toURI().toURL(), file, loop, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return null;
        }
        soundSystem.play(source);
        return source;
    }
    
    public String playTemporarySound(String file, boolean loop) {
        try {
            return soundSystem.quickPlay(false, new File(soundPath, file).toURI().toURL(), file, loop, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public String playTemporaryStreamingSound(String file, boolean loop) {
        try {
            return soundSystem.quickStream(false, new File(soundPath, file).toURI().toURL(), file, loop, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public void stopSound(String source) {
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
