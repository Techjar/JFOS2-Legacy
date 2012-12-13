package com.techjar.network.handler;

import com.techjar.jfos2.Constants;
import com.techjar.jfos2.server.Server;
import com.techjar.network.*;
import com.techjar.network.packet.*;

/**
 *
 * @author Techjar
 */
public class NetHandlerLogin extends NetHandler {
    @Override
    public boolean isServer() {
        return true;
    }
    
    @Override
    public void handleNetworkShutdown(String reason, Object[] info) {
        System.out.println(reason);
        for (int i = 0; i < info.length; i++) { System.out.println(info[i].toString()); }
    }
    
    @Override
    public void handleLogin(Packet1Login packet) {
        NetworkServer.pendingConn.remove(netManager);
        netManager.user = new NetworkUser(netManager, packet.name);
        
        if (packet.version < Constants.VERSION) {
            netManager.user.kick("Outdated client!");
            return;
        }
        if (packet.version > Constants.VERSION) {
            netManager.user.kick("Outdated server!");
            return;
        }
        if (NetworkUser.findExact(packet.name) != null) {
            netManager.user.kick("Username is already taken!");
            return;
        }
        
        netManager.queuePacket(new Packet1Login(Constants.VERSION, Server.server.getName(), false));
        netManager.setNetHandler(new NetHandlerServer());
        NetworkUser.users.add(netManager.user);
        
        for (int i = 0; i < NetworkUser.users.size(); i++) {
            NetworkUser user = NetworkUser.users.get(i);
            if (!user.username.equals(packet.name)) {
                netManager.queuePacket(new Packet4UserList(user.username, true));
            }
        }
        NetworkUser.globalPacket(new Packet4UserList(packet.name, true));
        NetworkUser.globalMessage(new StringBuilder(packet.name).append(" has joined the chat.").toString());
    }
    
    @Override
    public void handlePassword(Packet2Password packet) {
        registerPacket(packet);
    }
}
