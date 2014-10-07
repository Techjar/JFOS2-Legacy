package com.techjar.jfos2.client;

import static org.lwjgl.opengl.GL11.*;

import com.techjar.jfos2.util.Util;
import java.util.Stack;
import org.lwjgl.util.Color;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Techjar
 */
public final class RenderHelper {
    private static final Stack<Rectangle> scissorStack = new Stack<>();

    private RenderHelper() {
    }

    public static void setGlColor(Color color) {
        glColor4ub(color.getRedByte(), color.getGreenByte(), color.getBlueByte(), color.getAlphaByte());
    }

    public static void drawSquare(float x, float y, float width, float height, Color color, Texture texture) {
        if (texture == null) glDisable(GL_TEXTURE_2D);
        glTranslatef(x, y, 0);
        if (color != null) {
            setGlColor(color);
        } else glColor3f(1, 1, 1);
        glBegin(GL_QUADS);
        if (texture != null) glTexCoord2f(0, 0); glVertex2f(0, 0);
        if (texture != null) glTexCoord2f(texture.getWidth(), 0); glVertex2f(width, 0);
        if (texture != null) glTexCoord2f(texture.getWidth(), texture.getHeight()); glVertex2f(width, height);
        if (texture != null) glTexCoord2f(0, texture.getHeight()); glVertex2f(0, height);
        glEnd();
        glTranslatef(-x, -y, 0);
        if (texture == null) glEnable(GL_TEXTURE_2D);
    }
    
    public static void drawSquare(float x, float y, float width, float height, Color color) {
        drawSquare(x, y, width, height, color, null);
    }
    
    public static void drawSquare(float x, float y, float width, float height, Texture texture) {
        drawSquare(x, y, width, height, null, texture);
    }
    
    public static void drawBorder(float x, float y, float width, float height, float thickness, Color color, boolean top, boolean bottom, boolean left, boolean right) {
        if (left) drawSquare(x, y, thickness, height, color);
        if (top) drawSquare(x, y, width, thickness, color);
        if (right) drawSquare(x + width - thickness, y, thickness, height, color);
        if (bottom) drawSquare(x, y + height - thickness, width, thickness, color);
    }

    public static void drawBorder(float x, float y, float width, float height, float thickness, Color color) {
        drawBorder(x, y, width, height, thickness, color, true, true, true, true);
    }

    private static void performScissor(Rectangle rect) {
        glScissor((int)rect.getX(), Client.getInstance().getHeight() - (int)rect.getHeight() - (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
    }
    
    public static void beginScissor(Rectangle rect, boolean clipToPrevious) {
        if (scissorStack.empty()) glEnable(GL_SCISSOR_TEST);
        else if (clipToPrevious) rect = Util.clipRectangle(rect, scissorStack.peek());
        performScissor(scissorStack.push(rect));
    }

    public static void beginScissor(Rectangle rect) {
        beginScissor(rect, true);
    }
    
    public static void endScissor() {
        if (!scissorStack.empty()) {
            performScissor(scissorStack.pop());
            if (scissorStack.empty()) glDisable(GL_SCISSOR_TEST);
        }
    }

    public static Rectangle getPreviousScissor() {
        if (!scissorStack.empty()) {
            Rectangle prev = scissorStack.peek();
            return new Rectangle(prev.getX(), prev.getY(), prev.getWidth(), prev.getHeight());
        }
        return null;
    }
}
