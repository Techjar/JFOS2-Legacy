
package com.techjar.jfos2.client.gui.screen;

import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.client.gui.GUIBox;
import com.techjar.jfos2.client.gui.GUICallback;
import org.lwjgl.input.Controller;

/**
 *
 * @author Techjar
 */
public abstract class Screen {
    protected GUIBox container;
    protected GUICallback resizeHandler;
    protected boolean visible = true;
    protected boolean enabled = true;
    protected boolean removeRequested;

    public Screen() {
        container = new GUIBox();
        container.setDimension(Client.getInstance().getWidth(), Client.getInstance().getHeight());
        Client.getInstance().addResizeHandler(resizeHandler = new GUICallback() {
            @Override
            public void run() {
                container.setDimension(Client.getInstance().getWidth(), Client.getInstance().getHeight());
            }
        });
    }

    public boolean isRemoveRequested() {
        return removeRequested;
    }

    public GUIBox getContainer() {
        return container;
    }

    public boolean processKeyboardEvent() {
        return container.processKeyboardEvent();
    }

    public boolean processMouseEvent() {
        return container.processMouseEvent();
    }

    public boolean processControllerEvent(Controller controller) {
        return container.processControllerEvent(controller);
    }

    public void update(float delta) {
        container.update(delta);
    }

    public void render() {
        container.render();
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void remove() {
        Client.getInstance().removeResizeHandler(resizeHandler);
        removeRequested = true;
    }
}
