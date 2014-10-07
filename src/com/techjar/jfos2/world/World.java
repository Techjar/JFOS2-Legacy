
package com.techjar.jfos2.world;

import com.techjar.jfos2.entity.Entity;
import com.techjar.jfos2.util.Vector2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.newdawn.slick.geom.Shape;

/**
 *
 * @author Techjar
 */
public abstract class World {
    protected List<Entity> entityList = new ArrayList<>();
    protected List<Entity> toAddEntityList = new LinkedList<>();
    protected Map<Integer, Entity> entityMap = new HashMap<>();
    public void update(float delta) {
        if (toAddEntityList.size() > 0) {
            for (Entity entity : toAddEntityList) {
                internalAddEntity(entity);
            }
            toAddEntityList.clear();
        }
    }

    public void render() {
        throw new UnsupportedOperationException("Not a renderable world.");
    }

    public int getEntityCount() {
        return entityList.size();
    }

    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entityList);
    }

    public List<Entity> getEntitiesWithinShape(Shape shape, Class<? extends Entity> type, Entity exclude) {
        List<Entity> list = new ArrayList<>();
        Vector2 shapeCenter = new Vector2(shape.getCenterX(), shape.getCenterY());
        for (Entity entity : entityList) {
            if (entity != exclude && (type == null || type.isAssignableFrom(entity.getClass()))) {
                float distance = shapeCenter.distanceSquared(entity.getPosition());
                float combinedRadius = shape.getBoundingCircleRadius() + entity.getBoundingBox().getBoundingCircleRadius();
                if (combinedRadius * combinedRadius <= distance && shape.intersects(entity.getBoundingBox())) {
                    list.add(entity);
                }
            }
        }
        return list;
    }

    public List<Entity> getEntitiesWithinShape(Shape shape, Class<? extends Entity> type) {
        return getEntitiesWithinShape(shape, type, null);
    }

    public List<Entity> getEntitiesWithinShape(Shape shape, Entity exclude) {
        return getEntitiesWithinShape(shape, null, exclude);
    }

    public List<Entity> getEntitiesWithinShape(Shape shape) {
        return getEntitiesWithinShape(shape, null, null);
    }

    public Entity getEntity(int id) {
        return entityMap.get(id);
    }

    public void addEntity(Entity entity) {
        toAddEntityList.add(entity);
    }

    protected void internalAddEntity(Entity entity) {
        if (entityMap.containsKey(entity.getId())) throw new IllegalArgumentException("Entity ID already in use!");
        entity.setWorld(this);
        entityMap.put(entity.getId(), entity);
        entityList.add(entity);
    }

    public void removeEntity(Entity entity) {
        if (!entityMap.containsKey(entity.getId())) return;
        entity.setWorld(null);
        entityMap.remove(entity.getId());
        entityList.remove(entity.getId());
    }

    public void removeEntity(int id) {
        if (!entityMap.containsKey(id)) return;
        entityMap.remove(id);
        Iterator<Entity> it = entityList.iterator();
        Entity entity;
        while (it.hasNext()) {
            entity = it.next();
            if (entity.getId() == id) {
                entity.setWorld(null);
                it.remove();
                break;
            }
        }
    }
}
