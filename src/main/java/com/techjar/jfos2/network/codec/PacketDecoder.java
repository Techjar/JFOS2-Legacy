
package com.techjar.jfos2.network.codec;

import com.techjar.jfos2.network.Packet;
import com.techjar.jfos2.network.PacketBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.util.List;

/**
 * @author Techjar
 */
public class PacketDecoder extends ByteToMessageDecoder {
	private final boolean server;

	public PacketDecoder(boolean server) {
		this.server = server;
	}

	@Override
	protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) throws IOException {
		int length = in.readableBytes();
		if (length > 0) {
			PacketBuffer buffer = new PacketBuffer(in);
			int id = buffer.readVarInt();
			Packet packet = Packet.generatePacket(id, server);
			if (packet == null) {
				throw new IOException("Invalid packet ID: " + id);
			}

			packet.readData(buffer);
			if (buffer.readableBytes() > 0) {
				throw new IOException("Packet bytes larger than expected: " + buffer.readableBytes() + " extra bytes while reading " + packet.getClass().getSimpleName());
			}
			out.add(packet);
		}
	}
}
