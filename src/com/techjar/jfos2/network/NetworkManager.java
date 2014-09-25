
package com.techjar.jfos2.network;

import com.google.common.collect.Queues;
import com.techjar.jfos2.network.handler.NetHandler;
import com.techjar.jfos2.network.handler.NetHandlerServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.Queue;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Techjar
 */
public class NetworkManager extends SimpleChannelInboundHandler<Packet> {
    @Getter @Setter private NetHandler handler;
    private Queue<Packet> recieveQueue = Queues.newConcurrentLinkedQueue();

    @Override
    protected void channelRead0(ChannelHandlerContext context, Packet packet) throws Exception {
        if (packet.hasPriority()) {
            if (handler.isServer()) {
                packet.processServer(handler);
            } else {
                packet.processClient(handler);
            }
        } else {
            recieveQueue.add(packet);
        }
    }

    public void processRecieveQueue() {
        if (handler != null) {
            for (int i = 0; !recieveQueue.isEmpty() && i < 1000; i++) {
                if (handler.isServer()) {
                    recieveQueue.poll().processServer(handler);
                } else {
                    recieveQueue.poll().processClient(handler);
                }
            }
        }
    }
}
