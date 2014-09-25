
package com.techjar.jfos2.world;

import com.techjar.jfos2.entity.Entity;
import java.util.Iterator;
import org.lwjgl.input.Controller;

/**
 *
 * @author Techjar
 */
public class ClientWorld extends World {
    @Override
    public void update(float delta) {
        Iterator<Entity> it = entityList.iterator();
        Entity entity;
        while (it.hasNext()) {
            entity = it.next();
            if (entity.isDead()) {
                it.remove();
            } else {
                entity.update(delta);
                entity.updateClient(delta);
            }
        }
    }

    @Override
    public void render() {
        for (Entity entity : entityList)
            entity.render();
    }

    public boolean processKeyboardEvent() {
        for (Entity entity : entityList) {
            if (!entity.isDead() && !entity.processKeyboardEvent()) return false;
        }
        return true;
    }

    public boolean processMouseEvent() {
        for (Entity entity : entityList) {
            if (!entity.isDead() && !entity.processMouseEvent()) return false;
        }
        return true;
    }

    public boolean processControllerEvent(Controller controller) {
        for (Entity entity : entityList) {
            if (!entity.isDead() && !entity.processControllerEvent(controller)) return false;
        }
        return true;
    }
}
