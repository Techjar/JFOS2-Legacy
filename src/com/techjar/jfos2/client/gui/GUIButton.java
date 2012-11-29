package com.techjar.jfos2.client.gui;

import com.techjar.jfos2.Util;
import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.client.RenderHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Techjar
 */
public class GUIButton extends GUIText {
    protected Color bgColor;
    protected Texture texture;
    protected GUICallback clickHandler;
    protected boolean textured;
    protected boolean pressed;
    protected boolean windowClose;
    
    public GUIButton(UnicodeFont font, Color color, Color bgColor, String text) {
        super(font, color, text);
        this.bgColor = bgColor;
    }
    
    public GUIButton(UnicodeFont font, Color color, Color bgColor, Texture texture, String text) {
        this(font, color, bgColor, text);
        this.texture = texture;
        this.textured = true;
    }
    
    public GUIButton(Color bgColor) {
        this(null, null, bgColor, "");
    }
    
    public GUIButton(Color bgColor, Texture texture) {
        this(null, null, bgColor, texture, "");
    }

    @Override
    public boolean processKeyboardEvent() {
        return super.processKeyboardEvent();
    }

    @Override
    public boolean processMouseEvent() {
        if (Mouse.getEventButton() == 0) {
            if (Mouse.getEventButtonState()) {
                Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
                if (checkMouseIntersect(!windowClose, box)) {
                    pressed = true;
                    Client.client.getSoundManager().playTemporarySound("ui/click.wav", false);
                    if (clickHandler != null) {
                        clickHandler.setComponent(this);
                        clickHandler.run();
                    }
                    return false;
                }
            }
            else pressed = false;
        }
        return true;
    }
    
    @Override
    public void update() {
        if (!Mouse.isButtonDown(0)) {
            Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
            if (checkMouseIntersect(!windowClose, box)) {
                if (!hovered) Client.client.getSoundManager().playTemporarySound("ui/rollover.wav", false);
                hovered = true;
            }
            else hovered = false;
        }
    }

    @Override
    public void render() {
        Color bgColor2 = new Color(bgColor);
        Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
        if (pressed) bgColor2 = Util.addColors(bgColor2, new Color(25, 25, 25));
        else if (checkMouseIntersect(!windowClose, box)) bgColor2 = Util.addColors(bgColor2, new Color(50, 50, 50));
        if (textured) texture.bind();
        RenderHelper.drawSquare(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight(), bgColor2, textured);
        if (font != null && color != null) font.drawString(getPosition().getX() + ((dimension.getWidth() - font.getWidth(text.toString())) / 2), getPosition().getY() + ((dimension.getHeight() - font.getHeight(text.toString())) / 2), text.toString(), Util.convertColor(color));
    }

    public Color getBackgroundColor() {
        return bgColor;
    }

    public void setBackgroundColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public GUICallback getClickHandler() {
        return clickHandler;
    }

    public void setClickHandler(GUICallback clickHandler) {
        this.clickHandler = clickHandler;
    }
}
