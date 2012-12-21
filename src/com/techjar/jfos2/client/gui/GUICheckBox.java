package com.techjar.jfos2.client.gui;

import com.techjar.jfos2.Util;
import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.client.RenderHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.Rectangle;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Techjar
 */
public class GUICheckBox extends GUI {
    protected Color color;
    protected GUIBackground guiBg;
    protected Texture checkmark;
    protected GUICallback changeHandler;
    protected GUILabel label;

    protected boolean checked;

    public GUICheckBox(Color color, GUIBackground guiBg) {
        this.color = color;
        this.guiBg = guiBg;
        this.guiBg.setParent(this);
        this.checkmark = Client.client.getTextureManager().getTexture("ui/checkmark.png");
    }

    @Override
    public boolean processKeyboardEvent() {
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        if (Mouse.getEventButtonState()) {
            if (Mouse.getEventButton() == 0) {
                if (checkMouseIntersect(getComponentBox()) || (label != null && checkMouseIntersect(label.getComponentBox()))) {
                    Client.client.getSoundManager().playEffect("ui/click.wav", false);
                    setChecked(!checked);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void update() {
        if (!Mouse.isButtonDown(0)) {
            if (checkMouseIntersect(getComponentBox()) || (label != null && checkMouseIntersect(label.getComponentBox()))) {
                if (!hovered) Client.client.getSoundManager().playEffect("ui/rollover.wav", false);
                hovered = true;
            }
            else hovered = false;
        }
    }

    @Override
    public void render() {
        if (hovered) {
            Color color2 = new Color(guiBg.getBorderColor());
            guiBg.setBorderColor(Util.addColors(guiBg.getBorderColor(), new Color(50, 50, 50)));
            guiBg.render();
            guiBg.setBorderColor(color2);

            if (label != null) {
                color2 = label.getColor();
                label.setColor(Util.addColors(color2, new Color(50, 50, 50)));
                label.render();
                label.setColor(color2);
            }
        }
        else {
            guiBg.render();
            if (label != null) label.render();
        }
        if (checked) {
            checkmark.bind();
            RenderHelper.drawSquare(getPosition().getX() + guiBg.getBorderSize() + 3, getPosition().getY() + guiBg.getBorderSize() + 3, dimension.getWidth() - (guiBg.getBorderSize() * 2) - 6, dimension.getHeight() - (guiBg.getBorderSize() * 2) - 6, true, color);
        }
    }

    @Override
    public void setDimension(Dimension dimension) {
        super.setDimension(dimension);
        guiBg.setDimension(dimension);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        if (checked != this.checked) {
            this.checked = checked;
            if (changeHandler != null) {
                changeHandler.setComponent(this);
                changeHandler.run();
            }
        }
    }

    public GUILabel getLabel() {
        return label;
    }

    public void setLabel(GUILabel label) {
        this.label = label;
    }
}
