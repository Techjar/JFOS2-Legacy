package com.techjar.jfos2.client.gui;

import com.techjar.jfos2.Util;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;

/**
 *
 * @author Techjar
 */
public class GUIInputOption extends GUI {
    protected UnicodeFont font;
    protected Color color;
    protected GUIBackground guiBg;
    protected ButtonInfo button;

    protected boolean assign;

    public GUIInputOption(UnicodeFont font, Color color, GUIBackground guiBg) {
        this.font = font;
        this.color = color;
        this.guiBg = guiBg;
    }

    public GUIInputOption(UnicodeFont font, Color color) {
        this(font, color, null);
    }

    @Override
    public boolean processKeyboardEvent() {
        if (assign && Keyboard.getEventKeyState()) {
            button = new ButtonInfo(false, Keyboard.getEventKey());
            return false;
        }
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        if (Mouse.getEventButtonState()) {
            if (assign) {
                button = new ButtonInfo(true, Mouse.getEventButton());
            }
            else if (Mouse.getEventButton() == 0) {
                Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
                if (checkMouseIntersect(box)) {
                    assign = true;
                    return false;
                }
                else assign = false;
            }
        }
        return true;
    }

    @Override
    public void render() {
        font.drawString(getPosition().getX(), getPosition().getY(), button.isMouse() ? Mouse.getButtonName(button.getButton()) : Keyboard.getKeyName(button.getButton()), Util.convertColor(color));
    }

    @Override
    public void update() {
    }

    public ButtonInfo getButton() {
        return button;
    }

    public void setButton(boolean mouse, int button) {
        this.button = new ButtonInfo(mouse, button);
    }

    public class ButtonInfo {
        private boolean mouse;
        private int button;

        public ButtonInfo(boolean mouse, int button) {
            this.mouse = mouse;
            this.button = button;
        }

        public boolean isMouse() {
            return mouse;
        }

        public int getButton() {
            return button;
        }
    }
}
