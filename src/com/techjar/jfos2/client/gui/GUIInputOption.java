package com.techjar.jfos2.client.gui;

import com.techjar.jfos2.Util;
import com.techjar.jfos2.client.Client;
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
    protected GUICallback changeHandler;

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
            if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) button = null;
            else button = new ButtonInfo(false, Keyboard.getEventKey());
            if (changeHandler != null) {
                changeHandler.setComponent(this);
                changeHandler.run();
            }
            assign = false;
            return false;
        }
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        if (Mouse.getEventButtonState()) {
            if (assign) {
                button = new ButtonInfo(true, Mouse.getEventButton());
                if (changeHandler != null) {
                    changeHandler.setComponent(this);
                    changeHandler.run();
                }
                assign = false;
                return false;
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
    public void update() {
        if (!Mouse.isButtonDown(0) && !assign) {
            Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
            if (checkMouseIntersect(box)) {
                if (!hovered) Client.client.getSoundManager().playTemporarySound("ui/rollover.wav", false);
                hovered = true;
            }
            else hovered = false;
        }
    }

    @Override
    public void render() {
        int posAdd = 0;
        if (guiBg != null) {
            guiBg.render();
            posAdd = guiBg.getBorderSize() + 2;
        }
        Color color2 = color;
        if (hovered || assign) color2 = Util.addColors(color2, new Color(50, 50, 50));
        font.drawString(getPosition().getX() + posAdd, getPosition().getY() + posAdd, assign ? "_" : (button == null ? "None" : button.toString()), Util.convertColor(color2));
    }

    public ButtonInfo getButton() {
        return button;
    }

    public void setButton(boolean mouse, int button) {
        this.button = new ButtonInfo(mouse, button);
        if (changeHandler != null) {
            changeHandler.setComponent(this);
            changeHandler.run();
        }
    }

    public void setButton(String button) {
        this.button = ButtonInfo.fromString(button);
        if (changeHandler != null) {
            changeHandler.setComponent(this);
            changeHandler.run();
        }
    }

    public GUICallback getChangeHandler() {
        return changeHandler;
    }

    public void setChangeHandler(GUICallback changeHandler) {
        this.changeHandler = changeHandler;
    }

    public static class ButtonInfo {
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

        public static ButtonInfo fromString(String name) {
            if (Mouse.getButtonIndex(name) != -1) {
                return new ButtonInfo(true, Mouse.getButtonIndex(name));
            }
            if (Keyboard.getKeyIndex(name) != Keyboard.KEY_NONE) {
                return new ButtonInfo(false, Keyboard.getKeyIndex(name));
            }
            return null;
        }

        @Override
        public String toString() {
            return mouse ? Mouse.getButtonName(button) : Keyboard.getKeyName(button);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ButtonInfo other = (ButtonInfo) obj;
            if (this.mouse != other.mouse) {
                return false;
            }
            if (this.button != other.button) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + (this.mouse ? 1 : 0);
            hash = 97 * hash + this.button;
            return hash;
        }
    }
}
