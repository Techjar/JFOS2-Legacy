
package com.techjar.jfos2.network.handler;

import com.techjar.jfos2.player.Player;
import com.techjar.jfos2.util.ChatMessage;

/**
 *
 * @author Techjar
 */
public interface NetHandler {
    public boolean isServer();
    public Player getPlayer();
    public void onDisconnect(ChatMessage message);
}
