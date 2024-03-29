
package com.techjar.jfos2.world;

import com.techjar.jfos2.entity.Entity;

import java.util.Iterator;

/**
 * @author Techjar
 */
public class WorldServer extends World {
	@Override
	public void update(float delta) {
		super.update(delta);
		Iterator<Entity> it = entityList.iterator();
		Entity entity;
		while (it.hasNext()) {
			entity = it.next();
			if (entity.isDead()) {
				it.remove();
				if (entity.getWorldChange() != null) {
					entity.getWorldChange().internalAddEntity(entity);
					entity.setWorldChange(null);
				}
			} else {
				entity.update(delta);
				entity.updateServer(delta);
			}
		}

		// Collision detection
		for (Entity entity1 : entityList) {
			if (entity1.isDead()) continue;
			if (!entity1.isInert()) {
				for (Entity entity2 : entityList) {
					if (entity1 != entity2 && !entity2.isInert() && !entity2.isDead() && entity1.canCollide(entity2) && entity2.canCollide(entity1)) {
						float distance = entity1.getPosition().distanceSquared(entity2.getPosition());
						float combinedRadius = entity1.getBoundingBox().getBoundingCircleRadius() + entity2.getBoundingBox().getBoundingCircleRadius();
						if (distance <= combinedRadius * combinedRadius && entity1.getBoundingBox().intersects(entity2.getBoundingBox())) {
							entity1.onCollide(entity2);
						}
					}
				}
			}
			entity1.updateBoundingBox();
		}
	}
}
