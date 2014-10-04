
package com.techjar.jfos2.world;

import com.techjar.jfos2.entity.Entity;
import java.util.Collections;
import java.util.Iterator;
import org.lwjgl.input.Controller;

/**
 *
 * @author Techjar
 */
public class WorldClient extends World {
    @Override
    public void update(float delta) {
        super.update(delta);
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
            if (!entity.isDead()) entity.render();
    }

    @Override
    protected void internalAddEntity(Entity entity) {
        super.internalAddEntity(entity);
        Collections.sort(entityList);
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
