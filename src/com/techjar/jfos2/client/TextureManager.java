package com.techjar.jfos2.client;

import static org.lwjgl.opengl.GL11.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.opengl.ImageDataFactory;
import static org.newdawn.slick.opengl.InternalTextureLoader.createTextureID;
import static org.newdawn.slick.opengl.InternalTextureLoader.get2Fold;
import org.newdawn.slick.opengl.LoadableImageData;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.opengl.renderer.SGL;

/**
 *
 * @author Techjar
 */
public class TextureManager {
    protected final File texturePath;
    protected Map<String, Texture> cache;
    protected Map<String, Image> imageCache;
    protected Texture lastBind;
    protected static final Constructor<Image> imageConstructor;

    static { // We have to reflectively initialize this to bypass the clampTexture() call...
        try {
            imageConstructor = Image.class.getDeclaredConstructor();
            imageConstructor.setAccessible(true);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public TextureManager() {
        texturePath = new File("resources/textures/");
        cache = new HashMap<>();
        imageCache = new HashMap<>();
    }
    
    public Texture getTexture(String file, int filter) {
        try {
            Texture cached = cache.get(file);
            if (cached != null) return cached;
            Texture tex = TextureLoader.getTexture(file.substring(file.indexOf('.') + 1).toLowerCase(), new FileInputStream(new File(texturePath, file)), filter);
            cache.put(file, tex);
            return tex;
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public Texture getTexture(String file) {
        return getTexture(file, GL_LINEAR);
    }

    public Image getImage(String file, int filter) {
        try {
            Image cached = imageCache.get(file);
            if (cached != null) return cached;
            Image img = imageConstructor.newInstance();
            img.setTexture(getTexture(file));
            imageCache.put(file, img);
            return img;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Image getImage(String file) {
        return getImage(file, GL_LINEAR);
    }
    
    public void unloadTexture(String file) {
        if (cache.containsKey(file)) {
            cache.remove(file).release();
            imageCache.remove(file);
        }
    }
    
    public void cleanup() {
        for (Texture texture : cache.values())
            texture.release();
        cache.clear();
    }
}
