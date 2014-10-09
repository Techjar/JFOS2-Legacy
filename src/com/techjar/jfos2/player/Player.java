
package com.techjar.jfos2.player;

import com.techjar.jfos2.network.NetworkManager;
import com.techjar.jfos2.world.World;

/**
 *
 * @author Techjar
 */
public abstract class Player {
    public abstract NetworkManager getNetworkManager();
    public abstract World getWorld();
}
