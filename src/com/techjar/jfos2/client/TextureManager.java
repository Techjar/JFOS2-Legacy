package com.techjar.jfos2.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

/**
 *
 * @author Techjar
 */
public class TextureManager {
    protected final File texturePath;
    protected Map<String, Texture> cache;
    protected Texture lastBind;
    
    public TextureManager() {
        texturePath = new File("resources/textures/");
        cache = new HashMap<>();
    }
    
    public Texture getTexture(String file, int filter) {
        try {
            Texture cached = cache.get(file);
            if (cached != null) return cached;
            Texture tex = TextureLoader.getTexture(file.substring(file.indexOf('.') + 1).toUpperCase(), new FileInputStream(new File(texturePath, file)), filter);
            cache.put(file, tex);
            return tex;
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public Texture getTexture(String file) {
        return getTexture(file, GL11.GL_LINEAR);
    }
    
    public void unloadTexture(String file) {
        if (cache.containsKey(file)) cache.remove(file).release();
    }
    
    public void cleanup() {
        for (Texture texture : cache.values())
            texture.release();
        cache.clear();
    }
}
