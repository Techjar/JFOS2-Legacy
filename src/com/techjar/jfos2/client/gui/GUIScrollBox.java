/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.techjar.jfos2.client.gui;

import com.techjar.jfos2.MathHelper;
import com.techjar.jfos2.Util;
import static org.lwjgl.opengl.GL11.*;

import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.client.RenderHelper;
import java.awt.Dimension;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Vector2f;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

/**
 *
 * @author Techjar
 */
public class GUIScrollBox extends GUIContainer {
    protected Color color;
    protected boolean scrollX = true;
    protected boolean scrollY = true;
    protected int scrollXIncrement;
    protected int scrollYIncrement;
    
    protected Vector2f scrollOffset = new Vector2f();
    protected Vector2f scrollOffsetStart = new Vector2f();
    protected Vector2f scrollbarOffsetStart = new Vector2f();
    protected Vector2f mouseStart = new Vector2f();
    protected int scrolling;

    public GUIScrollBox(Color color) {
        this.color = color;
    }
    
    @Override
    public boolean processKeyboardEvent() {
        return super.processKeyboardEvent();
    }

    @Override
    public boolean processMouseEvent() {
        if (!super.processMouseEvent()) return false;
        if (Mouse.getEventButton() == 0) {
            if (Mouse.getEventButtonState()) {
                Vector2f scrollbarOffset = getScrollbarOffset();
                float[] sizeFactor = getScrollbarSizeFactor();
                if (scrollX) {
                    Rectangle box = new Rectangle(getPosition().getX() + scrollbarOffset.getX() + 1, getPosition().getY() + dimension.getHeight() - 9, (int)((dimension.getWidth() - 2) * sizeFactor[0]) - (scrollX && scrollY ? 10 : 0), 8);
                    if (checkMouseIntersect(box)) {
                        scrolling = 1;
                        mouseStart.set(Client.client.getMousePos());
                        scrollOffsetStart.set(scrollOffset);
                        scrollbarOffsetStart.set(getScrollbarOffset());
                    }
                }
                if (scrollY) {
                    Rectangle box = new Rectangle(getPosition().getX() + dimension.getWidth() - 9, getPosition().getY() + scrollbarOffset.getY() + 1, 8, (int)((dimension.getHeight() - 2) * sizeFactor[1]) - (scrollX && scrollY ? 10 : 0));
                    if (checkMouseIntersect(box)) {
                        scrolling = 2;
                        mouseStart.set(Client.client.getMousePos());
                        scrollOffsetStart.set(scrollOffset);
                        scrollbarOffsetStart.set(getScrollbarOffset());
                    }
                }
            }
            else scrolling = 0;
        }
        if (scrolling == 0 && Mouse.getEventDWheel() != 0) {
            
        }
        return true;
    }

    @Override
    public void update() {
        if (scrolling != 0) {
            Vector2f mouseOffset = Vector2f.sub(Client.client.getMousePos(), mouseStart, null);
            int[] maxScrollOffset = getMaxScrollOffset();
            int[] maxScrollbarOffset = getMaxScrollbarOffset();
            if (scrolling == 1) {
                scrollOffset.setX(MathHelper.clamp(scrollOffsetStart.getX() + (mouseOffset.getX() * ((float)maxScrollOffset[0] / (float)maxScrollbarOffset[0])), 0, maxScrollOffset[0]));
            }
            else if (scrolling == 2) {
                scrollOffset.setY(MathHelper.clamp(scrollOffsetStart.getY() + (mouseOffset.getY() * ((float)maxScrollOffset[1] / (float)maxScrollbarOffset[1])), 0, maxScrollOffset[1]));
            }
        }
        super.update();
    }

    @Override
    public void render() {
        Vector2f scrollbarOffset = getScrollbarOffset();
        float[] sizeFactor = getScrollbarSizeFactor();
        if (scrollX) {
            Color color2 = new Color(color);
            Rectangle box = new Rectangle(getPosition().getX() + scrollbarOffset.getX() + 1, getPosition().getY() + dimension.getHeight() - 9, (int)((dimension.getWidth() - 2) * sizeFactor[0]) - (scrollX && scrollY ? 10 : 0), 8);
            if (scrolling == 1 || (scrolling == 0 && checkMouseIntersect(box))) {
                color2 = Util.addColors(color2, new Color(50, 50, 50));
            }
            RenderHelper.drawSquare(getPosition().getX() + scrollbarOffset.getX() + 1, getPosition().getY() + dimension.getHeight() - 9, (int)((dimension.getWidth() - 2) * sizeFactor[0]) - (scrollX && scrollY ? 10 : 0), 8, color2);
        }
        if (scrollY) {
            Color color2 = new Color(color);
            Rectangle box = new Rectangle(getPosition().getX() + dimension.getWidth() - 9, getPosition().getY() + scrollbarOffset.getY() + 1, 8, (int)((dimension.getHeight() - 2) * sizeFactor[1]) - (scrollX && scrollY ? 10 : 0));
            if (scrolling == 2 || (scrolling == 0 && checkMouseIntersect(box))) {
                color2 = Util.addColors(color2, new Color(50, 50, 50));
            }
            RenderHelper.drawSquare(getPosition().getX() + dimension.getWidth() - 9, getPosition().getY() + scrollbarOffset.getY() + 1, 8, (int)((dimension.getHeight() - 2) * sizeFactor[1]) - (scrollX && scrollY ? 10 : 0), color2);
        }
        if (scrollX && scrollY) {
            RenderHelper.drawSquare(getPosition().getX() + dimension.getWidth() - 9, getPosition().getY() + dimension.getHeight() - 9, 8, 8, color);
        }
        super.render();
    }

    @Override
    public Vector2f getContainerPosition() {
        return Vector2f.sub(super.getPosition(), scrollOffset, null);
    }

    @Override
    public Rectangle getContainerBox() {
        return new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth() - (scrollY ? 10 : 0), dimension.getHeight() - (scrollX ? 10 : 0));
    }

    //@Override
    protected void doGlScissor() {
        glScissor((int)getPosition().getX(), Client.client.getHeight() - (dimension.getHeight() - (scrollX ? 10 : 0)) - (int)getPosition().getY(), dimension.getWidth() - (scrollY ? 10 : 0), dimension.getHeight() - (scrollX ? 10 : 0));
    }

    public boolean getScrollX() {
        return scrollX;
    }

    public void setScrollX(boolean scrollX) {
        this.scrollX = scrollX;
    }

    public boolean getScrollY() {
        return scrollY;
    }

    public void setScrollY(boolean scrollY) {
        this.scrollY = scrollY;
    }

    public int getScrollXIncrement() {
        return scrollXIncrement;
    }

    public void setScrollXIncrement(int scrollXIncrement) {
        this.scrollXIncrement = scrollXIncrement;
    }

    public int getScrollYIncrement() {
        return scrollYIncrement;
    }

    public void setScrollYIncrement(int scrollYIncrement) {
        this.scrollYIncrement = scrollYIncrement;
    }
    
    public Vector2f getScrollbarOffset() {
        int[] maxOffset = getMaxScrollOffset();
        int[] maxBarOffset = getMaxScrollbarOffset();
        return new Vector2f((maxOffset[0] == 0 ? 0 : maxBarOffset[0] * (scrollOffset.getX() / (float)maxOffset[0])), (maxOffset[1] == 0 ? 0 : maxBarOffset[1] * (scrollOffset.getY() / (float)maxOffset[1])));
    }
    
    public int[] getMaxScrollOffset() {
        int[] maxOffset = new int[2];
        GUI bottom = getBottomComponent(), right = getRightComponent();
        if (scrollY && bottom != null) {
            maxOffset[1] = (int)MathHelper.clamp(bottom.getRawPosition().y + bottom.getDimension().getHeight() - dimension.getHeight() + 10, 0, Integer.MAX_VALUE);
        }
        if (scrollX && right != null) {
            maxOffset[0] = (int)MathHelper.clamp(bottom.getRawPosition().x + bottom.getDimension().getWidth() - dimension.getWidth() + 10, 0, Integer.MAX_VALUE);
        }
        return maxOffset;
    }
    
    public int[] getMaxScrollbarOffset() {
        float[] sizeFactor = getScrollbarSizeFactorInverse();
        int[] offset = new int[2];
        offset[0] = (int)(dimension.getWidth() * sizeFactor[0]);
        offset[1] = (int)(dimension.getHeight() * sizeFactor[1]);
        return offset;
    }
    
    public float[] getScrollbarSizeFactor() {
        float[] size = new float[]{1, 1};
        GUI bottom = getBottomComponent(), right = getRightComponent();
        if (scrollY && bottom != null) {
            float compPos = bottom.getRawPosition().y + bottom.getDimension().getHeight();
            if (compPos > 0 && compPos >= dimension.getHeight() - 10) size[1] = (float)(dimension.getHeight() - 10) / compPos;
        }
        if (scrollX && right != null) {
            float compPos = right.getRawPosition().x + right.getDimension().getWidth();
            if (compPos > 0 && compPos >= dimension.getWidth() - 10) size[0] = (float)(dimension.getWidth() - 10) / compPos;
        }
        return size;
    }
    
    public float[] getScrollbarSizeFactorInverse() {
        float[] size = getScrollbarSizeFactor();
        size[0] = 1 - size[0];
        size[1] = 1 - size[1];
        return size;
    }
    
    public GUI getBottomComponent() {
        GUI bottom = null;
        for (GUI gui : components) {
            if (bottom == null) bottom = gui;
            if (gui.getRawPosition().x > bottom.getRawPosition().x) bottom = gui;
        }
        return bottom;
    }
    
    public GUI getRightComponent() {
        GUI bottom = null;
        for (GUI gui : components) {
            if (bottom == null) bottom = gui;
            if (gui.getRawPosition().y > bottom.getRawPosition().y) bottom = gui;
        }
        return bottom;
    }
}
