package com.techjar.jfos2.client;

import com.techjar.jfos2.Util;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.util.Color;
import org.newdawn.slick.geom.Rectangle;

/**
 *
 * @author Techjar
 */
public final class RenderHelper {
    private static Stack<Rectangle> prevScissor = new ScissorStack<Rectangle>();

    public static void setGlColor(Color color) {
        glColor4ub(color.getRedByte(), color.getGreenByte(), color.getBlueByte(), color.getAlphaByte());
    }

    public static void drawSquare(float x, float y, float width, float height, Color color, boolean textured) {
        glPushMatrix();
        if (!textured) glDisable(GL_TEXTURE_2D);
        glTranslatef(x, y, 0);
        if (color != null) {
            setGlColor(color);
        }
        else glColor3f(1, 1, 1);
        glBegin(GL_QUADS);
            if (textured) glTexCoord2f(0, 0); glVertex2f(0, 0);
            if (textured) glTexCoord2f(1, 0); glVertex2f(width, 0);
            if (textured) glTexCoord2f(1, 1); glVertex2f(width, height);
            if (textured) glTexCoord2f(0, 1); glVertex2f(0, height);
        glEnd();
        if (!textured) glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }
    
    public static void drawSquare(float x, float y, float width, float height, boolean textured, Color color) {
        drawSquare(x, y, width, height, color, textured);
    }
    
    public static void drawSquare(float x, float y, float width, float height, Color color) {
        drawSquare(x, y, width, height, color, false);
    }
    
    public static void drawSquare(float x, float y, float width, float height, boolean textured) {
        drawSquare(x, y, width, height, null, textured);
    }
    
    public static void drawBorder(float x, float y, float width, float height, float thickness, Color color) {
        drawSquare(x, y, thickness, height, color);
        drawSquare(x, y, width, thickness, color);
        drawSquare(x + width - thickness, y, thickness, height, color);
        drawSquare(x, y + height - thickness, width, thickness, color);
    }
    
    public static void beginScissor(Rectangle rect, boolean clipToPrevious) {
        if (clipToPrevious) {
            Rectangle prev = prevScissor.peek();
            if (prev != null) rect = Util.clipRectangle(rect, prev);
        }
        prevScissor.push(rect);
        glEnable(GL_SCISSOR_TEST);
        glScissor((int)rect.getX(), Client.client.getHeight() - (int)rect.getHeight() - (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
    }

    public static void beginScissor(Rectangle rect) {
        beginScissor(rect, true);
    }
    
    public static void endScissor() {
        if (prevScissor.pop() == null) return;
        Rectangle prev = prevScissor.peek();
        if (prev != null) glScissor((int)prev.getX(), Client.client.getHeight() - (int)prev.getHeight() - (int)prev.getY(), (int)prev.getWidth(), (int)prev.getHeight());
        else if (prevScissor.empty()) glDisable(GL_SCISSOR_TEST);
    }

    public static Rectangle getPreviousScissor() {
        Rectangle prev = prevScissor.peek();
        if (prev != null) return new Rectangle(prev.getX(), prev.getY(), prev.getWidth(), prev.getHeight());
        return null;
    }

    public static class ScissorStack<E> extends Stack<E> {
        @Override
        public synchronized E peek() {
            return this.empty() ? null : super.peek();
        }

        @Override
        public synchronized E pop() {
            return this.empty() ? null : super.pop();
        }
    }
}
