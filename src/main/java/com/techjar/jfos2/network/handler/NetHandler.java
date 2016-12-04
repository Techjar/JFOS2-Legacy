
package com.techjar.jfos2.network.handler;

import com.techjar.jfos2.network.ConnectionState;
import com.techjar.jfos2.network.NetworkManager;
import com.techjar.jfos2.network.NetworkSynchronizer;
import com.techjar.jfos2.player.Player;
import com.techjar.jfos2.util.ChatMessage;

/**
 * @author Techjar
 */
public interface NetHandler {
	public boolean isServer();

	public Player getPlayer();

	public NetworkManager getNetworkManager();

	public NetworkSynchronizer getNetworkSynchronizer();

	public void onDisconnect(ChatMessage message);

	public void onStateTransition(ConnectionState oldState, ConnectionState newState);
}
