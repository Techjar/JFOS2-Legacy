
package com.techjar.jfos2.network.handler;

import com.techjar.jfos2.player.Player;

/**
 *
 * @author Techjar
 */
public interface NetHandler {
    public boolean isServer();
    public Player getPlayer();
}
