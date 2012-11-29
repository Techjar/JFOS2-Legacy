/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
    
    public TextureManager() {
        texturePath = new File("resources/textures/");
        cache = new HashMap<String, Texture>();
    }
    
    public Texture getTexture(String file, int filter) {
        try {
            File file2 = new File(texturePath, file);
            String path = file2.getAbsolutePath();
            if (cache.containsKey(path)) return cache.get(path);
            return cache.put(path, TextureLoader.getTexture(path.substring(path.indexOf('.') + 1).toUpperCase(), new FileInputStream(file2), filter));
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public Texture getTexture(String file) {
        return getTexture(file, GL11.GL_LINEAR);
    }
    
    public void unloadTexture(String file) {
        File file2 = new File(texturePath, file);
        String path = file2.getAbsolutePath();
        if (cache.containsKey(path)) cache.remove(path).release();
    }
    
    public void cleanup() {
        for (Texture texture : cache.values())
            texture.release();
        cache.clear();
    }
}
