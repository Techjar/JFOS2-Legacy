/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.techjar.jfos2.client.gui;

import com.techjar.jfos2.client.RenderHelper;
import org.lwjgl.util.Color;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

/**
 *
 * @author Techjar
 */
public class GUIBackground extends GUI {
    protected Color bgColor;
    protected Color borderColor;
    protected int borderSize;

    public GUIBackground(Color bgColor, Color borderColor, int borderSize) {
        this.bgColor = bgColor;
        this.borderColor = borderColor;
        this.borderSize = borderSize;
    }
    
    public GUIBackground() {
        this(new Color(50, 50, 50), new Color(200, 0, 0), 2);
    }
    
    @Override
    public boolean processKeyboardEvent() {
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        return true;
    }

    @Override
    public void update() {
    }

    @Override
    public void render() {
        RenderHelper.drawSquare(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight(), bgColor);
        if (borderSize > 0) RenderHelper.drawBorder(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight(), borderSize, borderColor);
    }
    
    public Color getBackgroundColor() {
        return bgColor;
    }

    public void setBackgroundColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public int getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
    }
}
