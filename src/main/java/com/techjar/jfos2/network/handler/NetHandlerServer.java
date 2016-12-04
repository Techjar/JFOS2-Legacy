
package com.techjar.jfos2.network.handler;

import com.techjar.jfos2.player.Player;
import com.techjar.jfos2.player.PlayerServer;

/**
 * @author Techjar
 */
public abstract class NetHandlerServer implements NetHandler {
	private PlayerServer player;

	@Override
	public final boolean isServer() {
		return true;
	}

	@Override
	public Player getPlayer() {
		return player;
	}
}
