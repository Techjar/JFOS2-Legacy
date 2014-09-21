package com.techjar.jfos2.client;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import lombok.Lombok;
import lombok.SneakyThrows;
import org.newdawn.slick.Image;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

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

    static { // We have to reflectively initialize Image to bypass the clampTexture() call...
        try {
            imageConstructor = Image.class.getDeclaredConstructor();
            imageConstructor.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public TextureManager() {
        texturePath = new File("resources/textures/");
        cache = new HashMap<>();
        imageCache = new HashMap<>();
    }

    @SneakyThrows(IOException.class)
    public Texture getTexture(String file, int filter) {
        Texture cached = cache.get(file);
        if (cached != null) return cached;
        Texture tex = TextureLoader.getTexture(file.substring(file.indexOf('.') + 1).toLowerCase(), new FileInputStream(new File(texturePath, file)), filter);
        cache.put(file, tex);
        return tex;
    }
    
    public Texture getTexture(String file) {
        return getTexture(file, GL_LINEAR);
    }

    @SneakyThrows
    public Image getImage(String file, int filter) {
        Image cached = imageCache.get(file);
        if (cached != null) return cached;
        Image img = imageConstructor.newInstance();
        img.setTexture(getTexture(file));
        imageCache.put(file, img);
        return img;
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
