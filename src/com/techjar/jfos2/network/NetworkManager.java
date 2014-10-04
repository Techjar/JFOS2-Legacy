
package com.techjar.jfos2.network;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.techjar.jfos2.network.codec.PacketDecoder;
import com.techjar.jfos2.network.codec.PacketEncoder;
import com.techjar.jfos2.network.codec.PacketLengthDecoder;
import com.techjar.jfos2.network.codec.PacketLengthEncoder;
import com.techjar.jfos2.network.handler.NetHandler;
import com.techjar.jfos2.util.ChatMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Queue;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Techjar
 */
public class NetworkManager extends SimpleChannelInboundHandler<Packet> {
    public static final NioEventLoopGroup eventLoops = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Client IO #%d").setDaemon(true).build());
    @Getter private final boolean client;
    @Getter @Setter private NetHandler handler;
    private Queue<Packet> recieveQueue = Queues.newConcurrentLinkedQueue();
    private Queue<QueuedPacket> sendQueue = Queues.newConcurrentLinkedQueue();
    @Getter private Channel channel;
    @Getter private SocketAddress socketAddress;
    @Getter private ChatMessage shutdownReason;

    public NetworkManager(boolean client) {
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        super.channelActive(context);
        this.channel = context.channel();
        this.socketAddress = this.channel.remoteAddress();
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        //this.shutdown(new ChatMessage("#disconnect.endOfStream"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
        /*ChatMessage message;
        if (cause instanceof TimeoutException) {
            message = new ChatMessage("#disconnect.timeout");
        } else {
            if (client) message = new ChatMessage("#disconnect.clientError#: %s", cause.toString());
            else message = new ChatMessage("#disconnect.serverError#: %s", cause.toString());
        }
        this.shutdown(message);*/
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, Packet packet) throws Exception {
        if (packet.hasPriority()) {
            packet.process(handler);
        } else {
            recieveQueue.add(packet);
        }
    }

    public void sendPacket(Packet packet, GenericFutureListener... listeners) {
        if (channel != null && channel.isOpen()) {
            flushSendQueue();
            dispatchPacket(packet, listeners);
        } else {
            sendQueue.add(new QueuedPacket(packet, listeners));
        }
    }

    private void dispatchPacket(final Packet packet, final GenericFutureListener[] listeners) {
        if (channel.eventLoop().inEventLoop()) {
            channel.writeAndFlush(packet).addListeners(listeners).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            channel.eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    channel.writeAndFlush(packet).addListeners(listeners).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                }
            });
        }
    }

    private void flushSendQueue() {
        if (channel != null && channel.isOpen()) {
            while (!sendQueue.isEmpty()) {
                QueuedPacket queuedPacket = sendQueue.poll();
                dispatchPacket(queuedPacket.getPacket(), queuedPacket.getListeners());
            }
        }
    }

    public void processRecieveQueue() {
        if (handler != null) {
            for (int i = 0; !recieveQueue.isEmpty() && i < 1000; i++) {
                recieveQueue.poll().process(handler);
            }
        }
    }

    public void shutdown(ChatMessage reason) {
        if (channel.isOpen()) {
            channel.close();
            shutdownReason = reason;
        }
    }

    public boolean isChannelOpen() {
        return channel != null && channel.isOpen();
    }

    public boolean isChannelLocal() {
        return channel instanceof LocalChannel || channel instanceof LocalServerChannel;
    }

    public void disableAutoRead() {
        channel.config().setAutoRead(false);
    }

    public static NetworkManager startLANClient(InetAddress address, int port) {
        final NetworkManager manager = new NetworkManager(true);
        new Bootstrap().group(eventLoops).handler(new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.config().setOption(ChannelOption.IP_TOS, 24);
                channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                channel.pipeline().addLast("timeout", new ReadTimeoutHandler(20)).addLast("length_decoder", new PacketLengthDecoder()).addLast("decoder", new PacketDecoder()).addLast("length_encoder", new PacketLengthEncoder()).addLast("encoder", new PacketEncoder()).addLast("handler", manager);
            }
        }).channel(NioSocketChannel.class).connect(address, port).syncUninterruptibly();
        return manager;
    }

    public static NetworkManager startLocalClient(SocketAddress address) {
        final NetworkManager manager = new NetworkManager(true);
        new Bootstrap().group(eventLoops).handler(new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast("handler", manager);
            }
        }).channel(LocalChannel.class).connect(address).syncUninterruptibly();
        return manager;
    }

    @Data static class QueuedPacket {
        private final Packet packet;
        private final GenericFutureListener[] listeners;
    }
}
