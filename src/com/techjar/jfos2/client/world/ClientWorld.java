
package com.techjar.jfos2.client.world;

import com.techjar.jfos2.entity.Entity;
import org.lwjgl.input.Controller;

/**
 *
 * @author Techjar
 */
public class ClientWorld extends World {
    @Override
    public void update(double delta) {
        for (Entity entity : entityList) {
            entity.update(delta);
            entity.updateClient(delta);
        }
    }

    @Override
    public void render() {
        for (Entity entity : entityList)
            entity.render();
    }

    public boolean processKeyboardEvent() {
        for (Entity entity : entityList) {
            if (!entity.processKeyboardEvent()) return false;
        }
        return true;
    }

    public boolean processMouseEvent() {
        for (Entity entity : entityList) {
            if (!entity.processMouseEvent()) return false;
        }
        return true;
    }

    public boolean processControllerEvent(Controller controller) {
        for (Entity entity : entityList) {
            if (!entity.processControllerEvent(controller)) return false;
        }
        return true;
    }
}
