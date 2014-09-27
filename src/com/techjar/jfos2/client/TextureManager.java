package com.techjar.jfos2.client;


import static org.lwjgl.opengl.GL11.*;

import com.techjar.jfos2.util.Util;
import com.techjar.jfos2.util.json.TextureMeta;
import com.techjar.jfos2.util.json.TextureMeta.Animation.Frame;
import com.techjar.jfos2.util.logging.LogHelper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.Image;
import org.newdawn.slick.opengl.ImageDataFactory;
import org.newdawn.slick.opengl.LoadableImageData;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.TextureLoader;

/**
 *
 * @author Techjar
 */
public class TextureManager {
    protected final File texturePath;
    protected Map<String, Texture> cache;
    protected Map<String, TextureAnimated> animated;
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
        animated = new HashMap<>();
        imageCache = new HashMap<>();
    }

    public void update(float delta) {
        for (TextureAnimated texture : animated.values()) {
            texture.update(delta);
        }
    }

    @SneakyThrows(IOException.class)
    public Texture getTexture(String file, int filter) {
        Texture cached = cache.get(file);
        if (cached != null) return cached;
        Texture tex;
        String fileSub = file.substring(file.lastIndexOf('.') + 1).toLowerCase();
        @Cleanup FileInputStream inputStream = new FileInputStream(new File(texturePath, file));
        File metaFile = new File(texturePath, file + ".meta");
        if (metaFile.exists()) {
            @Cleanup FileReader fr = new FileReader(metaFile);
            TextureMeta meta = Util.GSON.fromJson(fr, TextureMeta.class);
            if (meta.animation != null) {
                if (meta.animation.frametime <= 0) {
                    throw new IllegalArgumentException("Frame time must be greater than zero.");
                }
                tex = loadAnimatedTexture(fileSub, inputStream, filter, meta);
                animated.put(file, (TextureAnimated)tex);
            } else {
                tex = TextureLoader.getTexture(fileSub, inputStream, filter);
            }
        } else {
            tex = TextureLoader.getTexture(fileSub, inputStream, filter);
        }
        cache.put(file, tex);
        return tex;
    }
    
    public Texture getTexture(String file) {
        return getTexture(file, GL_LINEAR);
    }

    @SneakyThrows(Exception.class)
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

    private TextureAnimated loadAnimatedTexture(String ref, InputStream in, int filter, TextureMeta meta) throws IOException { // , String resourceName, int target, int magFilter, int minFilter, boolean flipped, int[] transparent
        LoadableImageData imageData = ImageDataFactory.getImageDataFor(ref);
    	ByteBuffer imageBuffer = imageData.loadImage(new BufferedInputStream(in));

        boolean hasAlpha = imageData.getDepth() == 32;
        int componentCount = hasAlpha ? 4 : 3;
        //int pixelFormat = hasAlpha ? GL_RGBA : GL_RGB;
        int width = imageData.getWidth() / meta.animation.width;
        int height = imageData.getHeight() / meta.animation.height;
        int texWidth = Util.getNextPowerOfTwo(width);
        int texHeight = Util.getNextPowerOfTwo(height);

        // Copy data into a buffer with real image dimensions, not texture dimensions...
        ByteBuffer buffer = ByteBuffer.allocate(imageData.getWidth() * imageData.getHeight() * componentCount).order(ByteOrder.nativeOrder());
        byte[] rowBuffer = new byte[imageData.getWidth() * componentCount];
        for (int y = 0; y < imageData.getHeight(); y++) {
            imageBuffer.position(y * imageData.getTexWidth() * componentCount);
            imageBuffer.get(rowBuffer, 0, imageData.getWidth() * componentCount);
            buffer.put(rowBuffer);
        }
        buffer.rewind();

        int max = glGetInteger(GL_MAX_TEXTURE_SIZE);
        if (texWidth > max || texHeight > max) {
            throw new IOException("Attempted to allocate a texture too big for the current hardware.");
        }

        List<byte[]> textureData = new ArrayList<>();
        for (int y = 0; y < imageData.getHeight(); y += height) {
            for (int x = imageData.getWidth() - width; x >= 0; x -= width) {
                byte[] bytes = new byte[texWidth * texHeight * componentCount];
                for (int y2 = 0; y2 < height; y2++) {
                    buffer.position(((y + y2) * imageData.getWidth() + x) * componentCount);
                    buffer.get(bytes, texWidth * y2 * componentCount, width * componentCount);
                }
                textureData.add(bytes);
            }
        }

        int textureID = glGenTextures();
        TextureAnimated texture = new TextureAnimated(ref, GL_TEXTURE_2D, textureID, meta, textureData, BufferUtils.createByteBuffer(texWidth * texHeight * componentCount));
    	texture.setTextureWidth(texWidth);
    	texture.setTextureHeight(texHeight);
        texture.setWidth(width);
        texture.setHeight(height);
        texture.setAlpha(hasAlpha);

        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
        texture.uploadCurrentFrame();

        return texture;
    }
    
    public void unloadTexture(String file) {
        if (cache.containsKey(file)) {
            cache.remove(file).release();
            animated.remove(file);
            imageCache.remove(file);
        }
    }
    
    public void cleanup() {
        for (Texture texture : cache.values())
            texture.release();
        cache.clear();
        animated.clear();
        imageCache.clear();
    }

    public class TextureAnimated extends TextureImpl {
        private ByteBuffer buffer;
        private List<byte[]> textureData;
        private Frame[] frameInfo;
        private int currentFrame;
        private float timeCounter;

        public TextureAnimated(String ref, int target, int textureID, TextureMeta meta, List<byte[]> textureData, ByteBuffer buffer) {
            super(ref, target, textureID);
            this.buffer = buffer;
            this.textureData = textureData;
            this.frameInfo = meta.animation.frames;
            for (Frame frame : frameInfo) {
                if (frame.index >= textureData.size()) {
                    throw new IllegalArgumentException("Invalid frame index: " + frame.index);
                }
                if (frame.time <= 0) {
                    frame.time = meta.animation.frametime;
                }
            }
        }

        public void update(float delta) {
            timeCounter += delta;
            int lastFrame = currentFrame;
            while (timeCounter >= frameInfo[currentFrame].time) {
                timeCounter -= frameInfo[currentFrame].time;
                currentFrame++;
                if (currentFrame >= frameInfo.length) {
                    currentFrame = 0;
                }
            }
            if (currentFrame != lastFrame) {
                uploadCurrentFrame();
            }
        }

        public void uploadCurrentFrame() {
            buffer.rewind();
            buffer.put(textureData.get(frameInfo[currentFrame].index));
            buffer.rewind();
            glBindTexture(GL_TEXTURE_2D, getTextureID());
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, getTextureWidth(), getTextureHeight(), 0, hasAlpha() ? GL_RGBA : GL_RGB, GL_UNSIGNED_BYTE, buffer);
        }
    }
}
