package com.techjar.jfos2.client.gui;

import org.lwjgl.util.Dimension;
import org.lwjgl.util.vector.Vector2f;
import org.newdawn.slick.geom.Rectangle;

/**
 *
 * @author Techjar
 */
public class GUIBox extends GUIContainer {
    protected GUIBackground guiBg;

    public GUIBox(GUIBackground guiBg) {
        super();
        this.guiBg = guiBg;
        if (this.guiBg != null) this.guiBg.setParent(this);
    }

    public GUIBox() {
        this(null);
    }

    @Override
    public void render() {
        if (guiBg != null) guiBg.render();
        super.render();
    }

    @Override
    public void setDimension(Dimension dimension) {
        super.setDimension(dimension);
        if (guiBg != null) guiBg.setDimension(dimension);
    }

    @Override
    public Rectangle getContainerBox() {
        if (guiBg != null) return new Rectangle(getPosition().getX() + guiBg.getBorderSize(), getPosition().getY() + guiBg.getBorderSize(), dimension.getWidth() - (guiBg.getBorderSize() * 2), dimension.getHeight() - (guiBg.getBorderSize() * 2));
        return (Rectangle)getComponentBox();
    }

    @Override
    public Vector2f getContainerPosition() {
        if (guiBg != null) return Vector2f.add(getPosition(), new Vector2f(guiBg.getBorderSize(), guiBg.getBorderSize()), null);
        return super.getContainerPosition();
    }
}
