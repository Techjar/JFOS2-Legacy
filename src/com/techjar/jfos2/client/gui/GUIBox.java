package com.techjar.jfos2.client.gui;

import org.lwjgl.util.Dimension;

/**
 *
 * @author Techjar
 */
public class GUIBox extends GUIContainer {
    protected GUIBackground guiBg;

    public GUIBox(GUIBackground guiBg) {
        super();
        this.guiBg = guiBg;
        this.guiBg.setParent(this);
    }

    @Override
    public void render() {
        if (guiBg != null) guiBg.render();
        super.render();
    }

    @Override
    public void setDimension(Dimension dimension) {
        super.setDimension(dimension);
        guiBg.setDimension(dimension);
    }
}
