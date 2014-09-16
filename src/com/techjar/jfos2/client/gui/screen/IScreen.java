
package com.techjar.jfos2.client.gui.screen;

import com.techjar.jfos2.client.gui.GUIContainer;
import org.lwjgl.opengl.DisplayMode;

/**
 *
 * @author Techjar
 */
public interface IScreen {
    public GUIContainer buildContainer(DisplayMode displayMode);
}
