
package com.techjar.jfos2.client.world;

import com.techjar.jfos2.entity.Entity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Techjar
 */
public abstract class World {
    protected List<Entity> entityList = new ArrayList<>();
    protected Map<Integer, Entity> entityMap = new HashMap<>();

    public abstract void update(float delta);
    public abstract void render();

    public int getEntityCount() {
        return entityList.size();
    }

    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entityList);
    }

    public Entity getEntity(int id) {
        return entityMap.get(id);
    }

    public void addEntity(Entity entity) {
        if (entityMap.containsKey(entity.getId())) throw new RuntimeException("Entity ID already in use!");
        entityMap.put(entity.getId(), entity);
        entityList.add(entity);
        Collections.sort(entityList);
    }

    public void removeEntity(Entity entity) {
        if (!entityMap.containsKey(entity.getId())) return;
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
                it.remove();
                break;
            }
        }
    }
}
