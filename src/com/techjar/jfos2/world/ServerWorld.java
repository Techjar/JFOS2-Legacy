
package com.techjar.jfos2.world;

import com.techjar.jfos2.entity.Entity;
import java.util.Iterator;

/**
 *
 * @author Techjar
 */
public class ServerWorld extends World {
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
                entity.updateServer(delta);
            }
        }
    }

    @Override
    public void render() {
        throw new UnsupportedOperationException("Not a renderable world.");
    }
}
