
package com.techjar.jfos2.world;

import com.techjar.jfos2.entity.Entity;
import com.techjar.jfos2.util.Vector2;
import java.util.Iterator;

/**
 *
 * @author Techjar
 */
public class WorldServer extends World {
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

        // Collision checking
        for (Entity entity1 : entityList) {
            if (entity1.isInert() || entity1.isDead()) continue;
            for (Entity entity2 : entityList) {
                if (entity1 != entity2 && !entity2.isInert() && !entity2.isDead() && entity1.canCollide(entity2)) {
                    float distance = entity1.getPosition().distanceSquared(entity2.getPosition());
                    float combinedRadius = entity1.getBoundingBox().getBoundingCircleRadius() + entity2.getBoundingBox().getBoundingCircleRadius();
                    if (combinedRadius * combinedRadius <= distance && entity1.getBoundingBox().intersects(entity2.getBoundingBox())) {
                        entity1.onCollide(entity2);
                    }
                }
            }
        }
    }

    @Override
    public void render() {
        throw new UnsupportedOperationException("Not a renderable world.");
    }
}
