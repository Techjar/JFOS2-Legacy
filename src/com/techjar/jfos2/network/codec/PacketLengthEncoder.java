
package com.techjar.jfos2.network.codec;

import com.techjar.jfos2.network.Packet;
import com.techjar.jfos2.network.PacketBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public class PacketLengthEncoder extends MessageToByteEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext context, ByteBuf in, ByteBuf out) throws IOException {
        int length = in.readableBytes();
        if (length > Packet.MAX_SIZE) {
            throw new IOException("Packet length larger than maximum allowed (" + length + " > " + Packet.MAX_SIZE + ")");
        }

        PacketBuffer buffer = new PacketBuffer(out);
        buffer.ensureWritable(PacketBuffer.getVarIntSize(length) + length);
        buffer.writeVarInt(length);
        buffer.writeBytes(in, in.readerIndex(), length);
    }
}
