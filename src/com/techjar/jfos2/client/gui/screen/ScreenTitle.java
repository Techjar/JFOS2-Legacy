
package com.techjar.jfos2.client.gui.screen;

import com.techjar.jfos2.client.gui.GUIBox;
import com.techjar.jfos2.client.gui.GUIContainer;
import org.lwjgl.opengl.DisplayMode;

/**
 *
 * @author Techjar
 */
public class ScreenTitle implements IScreen {
    @Override
    public GUIContainer buildContainer(DisplayMode displayMode) {
        GUIContainer con = new GUIBox();
        return con;
    }
}
