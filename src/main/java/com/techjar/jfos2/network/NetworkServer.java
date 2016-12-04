
package com.techjar.jfos2.network;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.techjar.jfos2.network.codec.PacketDecoder;
import com.techjar.jfos2.network.codec.PacketEncoder;
import com.techjar.jfos2.network.codec.PacketLengthDecoder;
import com.techjar.jfos2.network.codec.PacketLengthEncoder;
import com.techjar.jfos2.player.Player;
import com.techjar.jfos2.server.Server;
import com.techjar.jfos2.util.ChatMessage;
import com.techjar.jfos2.util.logging.LogHelper;
import com.techjar.jfos2.world.World;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Techjar
 */
public class NetworkServer {
	private static final NioEventLoopGroup eventLoops = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty IO #%d").setDaemon(true).build());
	private final List<ChannelFuture> endpoints = Collections.synchronizedList(new ArrayList<ChannelFuture>());
	private final List<NetworkManager> managers = Collections.synchronizedList(new ArrayList<NetworkManager>());
	private final Server server;
	private volatile boolean alive = true;

	public NetworkServer(Server server) {
		this.server = server;
	}

	public void addLANEndpoint(InetAddress address, int port) {
		synchronized (endpoints) {
			endpoints.add(new ServerBootstrap().channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer() {
				@Override
				protected void initChannel(Channel channel) throws Exception {
					channel.config().setOption(ChannelOption.IP_TOS, 24);
					channel.config().setOption(ChannelOption.TCP_NODELAY, true);
					channel.pipeline().addLast("timeout", new ReadTimeoutHandler(20)).addLast("length_decoder", new PacketLengthDecoder()).addLast("decoder", new PacketDecoder(true)).addLast("length_encoder", new PacketLengthEncoder()).addLast("encoder", new PacketEncoder());
					NetworkManager manager = new NetworkManager(false);
					manager.setHandler(null); // TODO
					managers.add(manager);
					channel.pipeline().addLast("handler", manager);
				}
			}).group(eventLoops).localAddress(address, port).bind().syncUninterruptibly());
		}
	}

	public SocketAddress addLocalEndpoint() {
		synchronized (endpoints) {
			ChannelFuture future = new ServerBootstrap().channel(LocalServerChannel.class).childHandler(new ChannelInitializer() {
				@Override
				protected void initChannel(Channel channel) throws Exception {
					NetworkManager manager = new NetworkManager(false);
					manager.setHandler(null); // TODO
					managers.add(manager);
					channel.pipeline().addLast("handler", manager);
				}
			}).group(eventLoops).localAddress(LocalAddress.ANY).bind().syncUninterruptibly();
			endpoints.add(future);
			return future.channel().localAddress();
		}
	}

	public void terminate() {
		this.alive = false;
		for (ChannelFuture endpoint : endpoints) {
			endpoint.channel().close().syncUninterruptibly();
		}
	}

	public void tick() {
		synchronized (managers) {
			Iterator<NetworkManager> it = managers.iterator();
			while (it.hasNext()) {
				final NetworkManager manager = it.next();
				if (!manager.isChannelOpen()) {
					it.remove();
					if (manager.getShutdownReason() != null) {
						manager.getHandler().onDisconnect(manager.getShutdownReason());
					} else if (manager.getHandler() != null) {
						manager.getHandler().onDisconnect(/*new ChatMessage("#disconnect.genericReason")*/new ChatMessage());
					}
				} else {
					try {
						manager.processRecieveQueue();
					} catch (Exception ex) {
						if (manager.isChannelLocal()) {
							throw ex;
						} else {
							LogHelper.error("Error handling packet for " + manager.getSocketAddress(), ex);
							final ChatMessage message = /*new ChatMessage("#disconnect.error")*/new ChatMessage(); // TODO
							manager.sendPacket(null, new GenericFutureListener() { // TODO
								@Override
								public void operationComplete(Future future) throws Exception {
									manager.shutdown(message);
								}
							});
							manager.disableAutoRead();
						}
					}
				}
			}
		}
	}

	public void sendPacketToAll(Packet packet, GenericFutureListener... listeners) {
		synchronized (managers) {
			for (NetworkManager manager : managers) {
				manager.sendPacket(packet, listeners);
			}
		}
	}

	public void sendPacketToAllExcluding(Packet packet, Player[] excludes, GenericFutureListener... listeners) {
		synchronized (managers) {
			outer:
			for (NetworkManager manager : managers) {
				for (Player player : excludes) {
					if (player.getNetworkManager() == manager) continue outer;
				}
				manager.sendPacket(packet, listeners);
			}
		}
	}

	public void sendPacketToAllExcluding(Packet packet, Player exclude, GenericFutureListener... listeners) {
		sendPacketToAllExcluding(packet, new Player[]{exclude}, listeners);
	}

	public void sendPacketToAllInWorld(Packet packet, World world, GenericFutureListener... listeners) {
		synchronized (managers) {
			for (NetworkManager manager : managers) {
				if (manager.getHandler().getPlayer().getWorld() != world) continue;
				manager.sendPacket(packet, listeners);
			}
		}
	}

	public void sendPacketToAllInWorldExcluding(Packet packet, World world, Player[] excludes, GenericFutureListener... listeners) {
		synchronized (managers) {
			outer:
			for (NetworkManager manager : managers) {
				if (manager.getHandler().getPlayer().getWorld() != world) continue;
				for (Player player : excludes) {
					if (player.getNetworkManager() == manager) continue outer;
				}
				manager.sendPacket(packet, listeners);
			}
		}
	}

	public void sendPacketToAllInWorldExcluding(Packet packet, World world, Player exclude, GenericFutureListener... listeners) {
		sendPacketToAllInWorldExcluding(packet, world, new Player[]{exclude}, listeners);
	}
}
