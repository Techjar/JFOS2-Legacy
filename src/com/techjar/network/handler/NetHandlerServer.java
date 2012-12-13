package com.techjar.network.handler;

import com.techjar.network.*;
import com.techjar.network.packet.*;

/**
 *
 * @author Techjar
 */
public class NetHandlerServer extends NetHandler {
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
    public void handleChat(Packet3Chat packet) {
        String sendMsg = new StringBuilder(netManager.user.username).append(": ").append(packet.message).toString();
        System.out.println(sendMsg);
        NetworkUser.globalMessage(sendMsg);
    }
    
    @Override
    public void handleNameChange(Packet5NameChange packet) {
        if (NetworkUser.findExact(packet.name) == null) {
            NetworkUser.globalPacket(new Packet4UserList(netManager.user.username, false));
            NetworkUser.globalPacket(new Packet4UserList(packet.name, true));
            
            String changeMsg = new StringBuilder(netManager.user.username).append(" is now known as ").append(packet.name).append('.').toString();
            System.out.println(changeMsg);
            NetworkUser.globalMessage(changeMsg);
            
            netManager.user.username = packet.name;
            netManager.queuePacket(new Packet5NameChange(packet.name, true));
        }
        else netManager.queuePacket(new Packet5NameChange(packet.name, false));
    }
    
    @Override
    public void handleDisconnect(Packet255Disconnect packet) {
        netManager.shutdown(packet.reason);
    }
}
