package com.techjar.jfos2.client.gui;

import com.techjar.jfos2.util.Util;
import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.client.RenderHelper;
import java.util.List;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.geom.Ellipse;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Techjar
 */
public class GUIRadioButton extends GUI {
    protected Color color;
    protected GUIBackground guiBg;
    protected GUICallback changeHandler;
    protected GUILabel label;
    protected Texture circle;
    protected boolean selected;

    public GUIRadioButton(Color color, GUIBackground guiBg) {
        this.color = color;
        this.guiBg = guiBg;
        this.guiBg.setParent(this);
        this.circle = Client.getInstance().getTextureManager().getTexture("ui/circle.png");
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
                    if (!selected) {
                        Client.getInstance().getSoundManager().playEffect("ui/click.wav", false);
                        setSelected(true);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void update(float delta) {
        //circle1.setLocation(getPosition().getX() + (dimension.getWidth() / 2), getPosition().getY() + (dimension.getHeight() / 2));
        //circle2.setLocation(getPosition().getX() + (dimension.getWidth() / 2), getPosition().getY() + (dimension.getHeight() / 2));
        //circle2.setLocation(getPosition().getX() + (dimension.getWidth() / 2), getPosition().getY() + (dimension.getHeight() / 2));
        if (!Mouse.isButtonDown(0)) {
            if (checkMouseIntersect(getComponentBox()) || (label != null && checkMouseIntersect(label.getComponentBox()))) {
                if (!hovered) Client.getInstance().getSoundManager().playEffect("ui/rollover.wav", false);
                hovered = true;
            }
            else hovered = false;
        }
    }

    @Override
    public void render() {
        if (label != null) {
            if (hovered) {
                Color color2 = label.getColor();
                label.setColor(Util.addColors(color2, new Color(50, 50, 50)));
                label.render();
                label.setColor(color2);
            }
            else label.render();
        }
        circle.bind();
        RenderHelper.drawSquare(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight(), hovered ? Util.addColors(guiBg.getBorderColor(), new Color(50, 50, 50)) : guiBg.getBorderColor(), circle);
        RenderHelper.drawSquare(getPosition().getX() + guiBg.getBorderSize(), getPosition().getY() + guiBg.getBorderSize(), dimension.getWidth() - (guiBg.getBorderSize() * 2), dimension.getHeight() - (guiBg.getBorderSize() * 2), guiBg.getBackgroundColor(), circle);
        if (selected) RenderHelper.drawSquare(getPosition().getX() + guiBg.getBorderSize() + 3, getPosition().getY() + guiBg.getBorderSize() + 3, dimension.getWidth() - (guiBg.getBorderSize() * 2) - 6, dimension.getHeight() - (guiBg.getBorderSize() * 2) - 6, color, circle);
    }

    @Override
    public void setDimension(Dimension dimension) {
        super.setDimension(dimension);
    }

    @Override
    public Shape getComponentBox() {
        return new Ellipse(getPosition().getX() + (dimension.getWidth() / 2f), getPosition().getY() + (dimension.getHeight() / 2f), dimension.getWidth() / 2f, dimension.getHeight() / 2f);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        if (selected != this.selected) {
            this.selected = selected;
            if (selected) {
                List<GUI> containerList = getContainerList();
                for (GUI gui : containerList) {
                    if (gui == this || !(gui instanceof GUIRadioButton)) continue;
                    ((GUIRadioButton)gui).selected = false;
                }
            }
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
