package com.techjar.jfos2.client.gui;

import com.techjar.jfos2.Util;
import org.lwjgl.util.Color;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

/**
 *
 * @author Techjar
 */
public class GUIText extends GUI {
    protected UnicodeFont font;
    protected Color color;
    protected StringBuilder text;

    public GUIText(UnicodeFont font, Color color, String text) {
        super();
        this.font = font;
        this.color = color;
        this.text = new StringBuilder(text);
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
        font.drawString(getPosition().getX(), getPosition().getY(), text.toString(), Util.convertColor(color));
    }
    
    public String getText() {
        return text.toString();
    }

    public void setText(String text) {
        this.text = new StringBuilder(text);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public UnicodeFont getFont() {
        return font;
    }

    public void setFont(UnicodeFont font) {
        this.font = font;
    }
}
