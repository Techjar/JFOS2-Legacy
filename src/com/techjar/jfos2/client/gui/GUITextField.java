package com.techjar.jfos2.client.gui;

import org.lwjgl.util.Color;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;
import com.techjar.jfos2.Util;
import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.client.RenderHelper;

/**
 *
 * @author Techjar
 */
public class GUITextField extends GUIText {
    protected GUIBackground guiBg;
    protected StringBuilder renderText;
    protected int maxLength = Short.MAX_VALUE;
    protected boolean focused;
    protected boolean canLoseFocus = true;
    protected GUICallback changeHandler;
    
    // Timing stuff
    protected long cursorLastMillis;
    protected boolean cursorState;
    protected long repeatLastMillis;
    protected long repeatLastMillis2;
    protected int repeatLastKey;
    protected char repeatLastChar;
    protected boolean repeatState;
    protected boolean repeatState2;
    
    public GUITextField(UnicodeFont font, Color color, GUIBackground guiBg, String text) {
        super(font, color, text);
        guiBg.setParent(this);
        this.guiBg = guiBg;
    }
    
    public GUITextField(UnicodeFont font, Color color, GUIBackground guiBg) {
        this(font, color, guiBg, "");
    }
    
    @Override
    public boolean processKeyboardEvent() {
        super.processKeyboardEvent();
        if (focused) {
            if (Keyboard.getEventKeyState()) {
                char ch = Keyboard.getEventCharacter();
                if (Util.isValidCharacter(ch) && text.length() < maxLength) {
                    text.append(ch);
                    calculateRenderText();
                    if (changeHandler != null) {
                        changeHandler.setComponent(this);
                        changeHandler.run();
                    }
                    repeatState = true;
                }
                else if (Keyboard.getEventKey() == Keyboard.KEY_BACK && text.length() > 0) {
                    text.deleteCharAt(text.length() - 1);
                    calculateRenderText();
                    if (changeHandler != null) {
                        changeHandler.setComponent(this);
                        changeHandler.run();
                    }
                    repeatState = true;
                }
                repeatLastKey = Keyboard.getEventKey();
                repeatLastChar = Keyboard.getEventCharacter();
                repeatLastMillis = Client.client.getTick().getTickMillis();
            }
            else if (Keyboard.getEventKey() == repeatLastKey || Keyboard.getEventCharacter() == repeatLastChar) {
                repeatState = false;
                repeatState2 = false;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        super.processMouseEvent();
        if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0) {
            Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
            if (checkMouseIntersect(box)) {
                focused = true;
                return false;
            }
            else if(focused && canLoseFocus)
                focused = false;
        }
        return true;
    }
    
    @Override
    public void update() {
        super.update();
        if (Client.client.getTick().getTickMillis() - cursorLastMillis >= 500) {
            cursorState = !cursorState;
            cursorLastMillis = Client.client.getTick().getTickMillis();
        }
        
        if (repeatState && Client.client.getTick().getTickMillis() - repeatLastMillis >= 500) {
            if (!repeatState2) {
                repeatLastMillis2 = Client.client.getTick().getTickMillis() + 200;
                repeatState2 = true;
            }
            if (Client.client.getTick().getTickMillis() - repeatLastMillis2 >= 50) {
                if (Util.isValidCharacter(repeatLastChar) && text.length() < maxLength) {
                    text.append(repeatLastChar);
                    calculateRenderText();
                    if (changeHandler != null) {
                        changeHandler.setComponent(this);
                        changeHandler.run();
                    }
                }
                else if (repeatLastKey == Keyboard.KEY_BACK && text.length() > 0) {
                    text.deleteCharAt(text.length() - 1);
                    calculateRenderText();
                    if (changeHandler != null) {
                        changeHandler.setComponent(this);
                        changeHandler.run();
                    }
                }
                repeatLastMillis2 = Client.client.getTick().getTickMillis();
            }
        }
    }

    @Override
    public void render() {
        Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
        if (checkMouseIntersect(box)) {
            Color borderColor2 = new Color(guiBg.getBorderColor());
            guiBg.setBorderColor(Util.addColors(guiBg.getBorderColor(), new Color(50, 50, 50)));
            guiBg.render();
            guiBg.setBorderColor(borderColor2);
        }
        else guiBg.render();
        font.drawString(getPosition().getX() + guiBg.getBorderSize() + 3, getPosition().getY() + guiBg.getBorderSize() + 3, renderText.toString(), Util.convertColor(color));
        if (focused && cursorState) RenderHelper.drawSquare(getPosition().getX() + font.getWidth(renderText.toString()) + guiBg.getBorderSize() + 3, getPosition().getY() + guiBg.getBorderSize() + 2, guiBg.getBorderSize(), dimension.getHeight() - (guiBg.getBorderSize() * 2 - 4), color);
    }

    public GUIBackground getGuiBackground() {
        return guiBg;
    }

    public void setGuiBackground(GUIBackground guiBg) {
        this.guiBg = guiBg;
    }
    
    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public GUICallback getChangeHandler() {
        return changeHandler;
    }

    public void setChangeHandler(GUICallback changeHandler) {
        this.changeHandler = changeHandler;
    }
    
    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean getCanLoseFocus() {
        return canLoseFocus;
    }

    public void setCanLoseFocus(boolean canLoseFocus) {
        this.canLoseFocus = canLoseFocus;
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        calculateRenderText();
        if (changeHandler != null) {
            changeHandler.setComponent(this);
            changeHandler.run();
        }
    }

    @Override
    public void setDimension(Dimension dimension) {
        super.setDimension(dimension);
        guiBg.setDimension(dimension);
        calculateRenderText();
    }
    
    protected void calculateRenderText() {
        renderText = new StringBuilder(text);
        while (renderText.length() > 0) {
            if (font.getWidth(renderText.toString()) <= dimension.getWidth() - 10)
                break;
            renderText.deleteCharAt(0);
        }
    }
}
