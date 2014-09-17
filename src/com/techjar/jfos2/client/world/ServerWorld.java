
package com.techjar.jfos2.client.world;

import com.techjar.jfos2.entity.Entity;

/**
 *
 * @author Techjar
 */
public class ServerWorld extends World {
    @Override
    public void update(double delta) {
        for (Entity entity : entityList) {
            entity.update(delta);
            entity.updateServer(delta);
        }
    }

    @Override
    public void render() {
        throw new UnsupportedOperationException("Not a renderable world.");
    }
}
