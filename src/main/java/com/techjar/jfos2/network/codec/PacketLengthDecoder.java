
package com.techjar.jfos2.network.codec;

import com.techjar.jfos2.network.Packet;
import com.techjar.jfos2.network.PacketBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

import lombok.Cleanup;

/**
 * @author Techjar
 */
public class PacketLengthDecoder extends ByteToMessageDecoder {
	@Override
	protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) throws Exception {
		in.markReaderIndex();
		byte[] bytes = new byte[5];
		for (int i = 0; i < bytes.length; i++) {
			if (!in.isReadable()) {
				in.resetReaderIndex();
				return;
			}
			bytes[i] = in.readByte();
			if (bytes[i] >= 0) {
				@Cleanup("release") PacketBuffer buffer = new PacketBuffer(Unpooled.wrappedBuffer(bytes));
				int length = buffer.readVarInt();
				if (length > Packet.MAX_SIZE) {
					throw new CorruptedFrameException("Recieved packet size larger than maximum allowed (" + length + " > " + Packet.MAX_SIZE + ")");
				}
				if (length < 0) {
					throw new CorruptedFrameException("Recieved packet size less than zero! Something broke!");
				}

				if (in.readableBytes() < length) {
					in.resetReaderIndex();
					return;
				} else {
					out.add(in.readBytes(length));
					return;
				}
			}
		}

		throw new CorruptedFrameException("length wider than 32-bit");
	}
}
