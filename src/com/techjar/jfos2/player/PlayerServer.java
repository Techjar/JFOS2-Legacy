
package com.techjar.jfos2.player;

import com.techjar.jfos2.network.NetworkManager;
import com.techjar.jfos2.world.World;

/**
 *
 * @author Techjar
 */
public class PlayerServer extends Player {
    @Override
    public NetworkManager getNetworkManager() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public World getWorld() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
