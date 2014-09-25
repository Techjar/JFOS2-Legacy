
package com.techjar.jfos2.network.handler;

import com.techjar.jfos2.player.Player;

/**
 *
 * @author Techjar
 */
public abstract class NetHandlerClient implements NetHandler {
    @Override
    public final boolean isServer() {
        return false;
    }

    @Override
    public Player getPlayer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
