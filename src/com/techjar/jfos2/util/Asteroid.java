package com.techjar.jfos2.util;

import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;

import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;

/**
 *
 * @author Techjar
 */
public class Asteroid {
    private static int framebuffer;
    private Shape[] shapes;
    private Shape collisionBox;
    private int texture;
    private double[] textureUV; // 4 elements: X Min, Y Min, X Max, Y Max
    private int textureWidth;
    private int textureHeight;

    public Asteroid(Shape body, Shape[] craters) {
        shapes = new Shape[craters.length + 2];
        Shape scaled = body.transform(Transform.createScaleTransform((body.getWidth() + 6) / body.getWidth(), (body.getHeight() + 6) / body.getHeight()));
        scaled.setCenterX(body.getCenterX());
        scaled.setCenterY(body.getCenterY());
        shapes[0] = scaled;
        shapes[1] = body;
        System.arraycopy(craters, 0, shapes, 2, craters.length);
    }

    public static void init(int fb) {
        if (framebuffer != 0) throw new IllegalStateException("Already initialized!");
        framebuffer = fb;
    }

    public Shape getCollisionBox() {
        if (collisionBox == null) {
            collisionBox = new Polygon(shapes[0].getPoints());
            collisionBox.setCenterX(0);
            collisionBox.setCenterY(0);
        }
        return collisionBox;
    }

    public void renderToTexture() {
        if (framebuffer == 0) throw new IllegalStateException("Not yet initialized!");
        if (texture != 0) throw new IllegalStateException("Already rendered to texture!");
        Shape shape = shapes[0];
        int width = Util.getNextPowerOfTwo((int)Math.ceil(shape.getWidth()));
        int height = Util.getNextPowerOfTwo((int)Math.ceil(shape.getHeight()));

        texture = glGenTextures();
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebuffer);
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 512, 512, 0, GL_RGBA, GL_INT, (ByteBuffer)null);
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, texture, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }

    public void render() {
        if (texture == 0) renderToTexture();
    }
}
