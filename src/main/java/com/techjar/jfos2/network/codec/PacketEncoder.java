
package com.techjar.jfos2.network.codec;

import com.techjar.jfos2.network.Packet;
import com.techjar.jfos2.network.PacketBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;

/**
 * @author Techjar
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {
	@Override
	protected void encode(ChannelHandlerContext context, Packet packet, ByteBuf out) throws IOException {
		Integer id = packet.getId();
		if (id == null) {
			throw new IOException("Can't encode unregistered packet: " + packet.getClass().getSimpleName());
		}

		PacketBuffer buffer = new PacketBuffer(out);
		buffer.writeVarInt(id);
		packet.writeData(buffer);
	}
}
