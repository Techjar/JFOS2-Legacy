
package com.techjar.jfos2.network;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

/**
 *
 * @author Techjar
 */
public class PacketBuffer extends ByteBuf {
    private static final int STRING_MAX_LENGTH = 32767;
    private final ByteBuf buffer;

    public PacketBuffer(ByteBuf buffer) {
        this.buffer = buffer;
    }

    /**
     * Returns the number of bytes needed for a variable length integer.
     * See <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">Google Protocol Buffers</a>
     *
     * @param value integer to check
     * @return number of bytes
     */
    public static int getVarIntSize(int value) {
        value = (value << 1) ^ (value >> 31);
        if ((value & (-1 << 7)) == 0) {
            return 1;
        } else if ((value & (-1 << 14)) == 0) {
            return 2;
        } else if ((value & (-1 << 21)) == 0) {
            return 3;
        } else if ((value & (-1 << 28)) == 0) {
            return 4;
        }
        return 5;
    }

    /**
     * Returns the number of bytes needed for a variable length long.
     * See <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">Google Protocol Buffers</a>
     *
     * @param value long to check
     * @return number of bytes
     */
    public static int getVarLongSize(long value) {
        value = (value << 1) ^ (value >> 63);
        if ((value & (-1L << 7)) == 0) {
            return 1;
        } else if ((value & (-1L << 14)) == 0) {
            return 2;
        } else if ((value & (-1L << 21)) == 0) {
            return 3;
        } else if ((value & (-1L << 28)) == 0) {
            return 4;
        } else if ((value & (-1L << 35)) == 0) {
            return 5;
        } else if ((value & (-1L << 42)) == 0) {
            return 6;
        } else if ((value & (-1L << 49)) == 0) {
            return 7;
        } else if ((value & (-1L << 56)) == 0) {
            return 8;
        }
        return 9;
    }

    /**
     * Reads a variable length integer from the buffer.
     * See <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">Google Protocol Buffers</a>
     *
     * @return integer read from buffer
     * @throws IOException if number of bytes read exceeds expected maximum
     */
    public int readVarInt() throws IOException {
        int value = 0;
        int i = 0;
        byte b;

        do {
            b = this.readByte();
            value |= (b & 0x7F) << i++ * 7;
            if (i > 5) {
                throw new IOException("Variable length number too large");
            }
        } while ((b & 0x80) == 0x80);

        int temp = (((value << 31) >> 31) ^ value) >> 1;
        return temp ^ (value & (1 << 31));
    }

    /**
     * Writes a variable length integer to the buffer.
     * See <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">Google Protocol Buffers</a>
     *
     * @param value integer to write
     * @return this ByteBuf
     */
    public ByteBuf writeVarInt(int value) {
        value = (value << 1) ^ (value >> 31);
        while ((value & (-1 << 7)) != 0) {
            this.writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }
        this.writeByte(value);
        return this;
    }

    /**
     * Reads a variable length long from the buffer.
     * See <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">Google Protocol Buffers</a>
     *
     * @return long read from buffer
     * @throws IOException if number of bytes read exceeds expected maximum
     */
    public long readVarLong() throws IOException {
        long value = 0;
        int i = 0;
        byte b;

        do {
            b = this.readByte();
            value |= (b & 0x7FL) << i++ * 7;
            if (i > 9) {
                throw new IOException("Variable length number too large");
            }
        } while ((b & 0x80) == 0x80);

        long temp = (((value << 63) >> 63) ^ value) >> 1;
        return temp ^ (value & (1L << 63));
    }

    /**
     * Writes a variable length long to the buffer.
     * See <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">Google Protocol Buffers</a>
     *
     * @param value long to write
     * @return this ByteBuf
     */
    public ByteBuf writeVarLong(long value) {
        value = (value << 1) ^ (value >> 63);
        while ((value & (-1L << 7)) != 0) {
            this.writeByte((int)(value & 0x7F | 0x80));
            value >>>= 7;
        }
        this.writeByte((int)value);
        return this;
    }

    public String readString() throws IOException {
        int length = this.readVarInt();
        if (length > STRING_MAX_LENGTH * 4) {
            throw new IOException("Recieved string bytes larger than maximum allowed (" + length + " > " + STRING_MAX_LENGTH * 4 + ")");
        }
        if (length < 0) {
            throw new IOException("Recieved string bytes less than zero! Something broke!");
        }

        String str = new String(readBytes(length).array(), Charsets.UTF_8);
        if (str.length() > STRING_MAX_LENGTH) {
            throw new IOException("Recieved string length longer than maximum allowed (" + length + " > " + STRING_MAX_LENGTH + ")");
        }
        return str;
    }

    public ByteBuf writeString(String value) throws IOException {
        if (value.length() > STRING_MAX_LENGTH) {
            throw new IOException("String length longer than maximum allowed (" + value.length() + " > " + STRING_MAX_LENGTH + ")");
        }
        byte[] bytes = value.getBytes(Charsets.UTF_8);
        this.writeVarInt(bytes.length);
        this.writeBytes(bytes);
        return this;
    }

    @Override
    public int capacity() {
        return buffer.capacity();
    }

    @Override
    public ByteBuf capacity(int newCapacity) {
        return buffer.capacity(newCapacity);
    }

    @Override
    public int maxCapacity() {
        return buffer.maxCapacity();
    }

    @Override
    public ByteBufAllocator alloc() {
        return buffer.alloc();
    }

    @Override
    public ByteOrder order() {
        return buffer.order();
    }

    @Override
    public ByteBuf order(ByteOrder endianness) {
        return buffer.order(endianness);
    }

    @Override
    public ByteBuf unwrap() {
        return buffer.unwrap();
    }

    @Override
    public boolean isDirect() {
        return buffer.isDirect();
    }

    @Override
    public int readerIndex() {
        return buffer.readerIndex();
    }

    @Override
    public ByteBuf readerIndex(int readerIndex) {
        return buffer.readerIndex(readerIndex);
    }

    @Override
    public int writerIndex() {
        return buffer.writerIndex();
    }

    @Override
    public ByteBuf writerIndex(int writerIndex) {
        return buffer.writerIndex(writerIndex);
    }

    @Override
    public ByteBuf setIndex(int readerIndex, int writerIndex) {
        return buffer.setIndex(readerIndex, writerIndex);
    }

    @Override
    public int readableBytes() {
        return buffer.readableBytes();
    }

    @Override
    public int writableBytes() {
        return buffer.writableBytes();
    }

    @Override
    public int maxWritableBytes() {
        return buffer.maxWritableBytes();
    }

    @Override
    public boolean isReadable() {
        return buffer.isReadable();
    }

    @Override
    public boolean isReadable(int size) {
        return buffer.isReadable(size);
    }

    @Override
    public boolean isWritable() {
        return buffer.isWritable();
    }

    @Override
    public boolean isWritable(int size) {
        return buffer.isWritable(size);
    }

    @Override
    public ByteBuf clear() {
        return buffer.clear();
    }

    @Override
    public ByteBuf markReaderIndex() {
        return buffer.markReaderIndex();
    }

    @Override
    public ByteBuf resetReaderIndex() {
        return buffer.resetReaderIndex();
    }

    @Override
    public ByteBuf markWriterIndex() {
        return buffer.markWriterIndex();
    }

    @Override
    public ByteBuf resetWriterIndex() {
        return buffer.resetWriterIndex();
    }

    @Override
    public ByteBuf discardReadBytes() {
        return buffer.discardReadBytes();
    }

    @Override
    public ByteBuf discardSomeReadBytes() {
        return buffer.discardSomeReadBytes();
    }

    @Override
    public ByteBuf ensureWritable(int minWritableBytes) {
        return buffer.ensureWritable(minWritableBytes);
    }

    @Override
    public int ensureWritable(int minWritableBytes, boolean force) {
        return buffer.ensureWritable(minWritableBytes, force);
    }

    @Override
    public boolean getBoolean(int index) {
        return buffer.getBoolean(index);
    }

    @Override
    public byte getByte(int index) {
        return buffer.getByte(index);
    }

    @Override
    public short getUnsignedByte(int index) {
        return buffer.getUnsignedByte(index);
    }

    @Override
    public short getShort(int index) {
        return buffer.getShort(index);
    }

    @Override
    public int getUnsignedShort(int index) {
        return buffer.getUnsignedShort(index);
    }

    @Override
    public int getMedium(int index) {
        return buffer.getMedium(index);
    }

    @Override
    public int getUnsignedMedium(int index) {
        return buffer.getUnsignedMedium(index);
    }

    @Override
    public int getInt(int index) {
        return buffer.getInt(index);
    }

    @Override
    public long getUnsignedInt(int index) {
        return buffer.getUnsignedInt(index);
    }

    @Override
    public long getLong(int index) {
        return buffer.getLong(index);
    }

    @Override
    public char getChar(int index) {
        return buffer.getChar(index);
    }

    @Override
    public float getFloat(int index) {
        return buffer.getFloat(index);
    }

    @Override
    public double getDouble(int index) {
        return buffer.getDouble(index);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf dst) {
        return buffer.getBytes(index, dst);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf dst, int length) {
        return buffer.getBytes(index, dst, length);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        return buffer.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public ByteBuf getBytes(int index, byte[] dst) {
        return buffer.getBytes(index, dst);
    }

    @Override
    public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
        return buffer.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuffer dst) {
        return buffer.getBytes(index, dst);
    }

    @Override
    public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
        return buffer.getBytes(index, out, length);
    }

    @Override
    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        return buffer.getBytes(index, out, length);
    }

    @Override
    public ByteBuf setBoolean(int index, boolean value) {
        return buffer.setBoolean(index, value);
    }

    @Override
    public ByteBuf setByte(int index, int value) {
        return buffer.setByte(index, value);
    }

    @Override
    public ByteBuf setShort(int index, int value) {
        return buffer.setShort(index, value);
    }

    @Override
    public ByteBuf setMedium(int index, int value) {
        return buffer.setMedium(index, value);
    }

    @Override
    public ByteBuf setInt(int index, int value) {
        return buffer.setInt(index, value);
    }

    @Override
    public ByteBuf setLong(int index, long value) {
        return buffer.setLong(index, value);
    }

    @Override
    public ByteBuf setChar(int index, int value) {
        return buffer.setChar(index, value);
    }

    @Override
    public ByteBuf setFloat(int index, float value) {
        return buffer.setFloat(index, value);
    }

    @Override
    public ByteBuf setDouble(int index, double value) {
        return buffer.setDouble(index, value);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf src) {
        return buffer.setBytes(index, src);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf src, int length) {
        return buffer.setBytes(index, src, length);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
        return buffer.setBytes(index, src, srcIndex, length);
    }

    @Override
    public ByteBuf setBytes(int index, byte[] src) {
        return buffer.setBytes(index, src);
    }

    @Override
    public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
        return buffer.setBytes(index, src, srcIndex, length);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuffer src) {
        return buffer.setBytes(index, src);
    }

    @Override
    public int setBytes(int index, InputStream in, int length) throws IOException {
        return buffer.setBytes(index, in, length);
    }

    @Override
    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        return buffer.setBytes(index, in, length);
    }

    @Override
    public ByteBuf setZero(int index, int length) {
        return buffer.setZero(index, length);
    }

    @Override
    public boolean readBoolean() {
        return buffer.readBoolean();
    }

    @Override
    public byte readByte() {
        return buffer.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return buffer.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return buffer.readShort();
    }

    @Override
    public int readUnsignedShort() {
        return buffer.readUnsignedShort();
    }

    @Override
    public int readMedium() {
        return buffer.readMedium();
    }

    @Override
    public int readUnsignedMedium() {
        return buffer.readUnsignedMedium();
    }

    @Override
    public int readInt() {
        return buffer.readInt();
    }

    @Override
    public long readUnsignedInt() {
        return buffer.readUnsignedInt();
    }

    @Override
    public long readLong() {
        return buffer.readLong();
    }

    @Override
    public char readChar() {
        return buffer.readChar();
    }

    @Override
    public float readFloat() {
        return buffer.readFloat();
    }

    @Override
    public double readDouble() {
        return buffer.readDouble();
    }

    @Override
    public ByteBuf readBytes(int length) {
        return buffer.readBytes(length);
    }

    @Override
    public ByteBuf readSlice(int length) {
        return buffer.readSlice(length);
    }

    @Override
    public ByteBuf readBytes(ByteBuf dst) {
        return buffer.readBytes(dst);
    }

    @Override
    public ByteBuf readBytes(ByteBuf dst, int length) {
        return buffer.readBytes(dst, length);
    }

    @Override
    public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
        return buffer.readBytes(dst, dstIndex, length);
    }

    @Override
    public ByteBuf readBytes(byte[] dst) {
        return buffer.readBytes(dst);
    }

    @Override
    public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
        return buffer.readBytes(dst, dstIndex, length);
    }

    @Override
    public ByteBuf readBytes(ByteBuffer dst) {
        return buffer.readBytes(dst);
    }

    @Override
    public ByteBuf readBytes(OutputStream out, int length) throws IOException {
        return buffer.readBytes(out, length);
    }

    @Override
    public int readBytes(GatheringByteChannel out, int length) throws IOException {
        return buffer.readBytes(out, length);
    }

    @Override
    public ByteBuf skipBytes(int length) {
        return buffer.skipBytes(length);
    }

    @Override
    public ByteBuf writeBoolean(boolean value) {
        return buffer.writeBoolean(value);
    }

    @Override
    public ByteBuf writeByte(int value) {
        return buffer.writeByte(value);
    }

    @Override
    public ByteBuf writeShort(int value) {
        return buffer.writeShort(value);
    }

    @Override
    public ByteBuf writeMedium(int value) {
        return buffer.writeMedium(value);
    }

    @Override
    public ByteBuf writeInt(int value) {
        return buffer.writeInt(value);
    }

    @Override
    public ByteBuf writeLong(long value) {
        return buffer.writeLong(value);
    }

    @Override
    public ByteBuf writeChar(int value) {
        return buffer.writeChar(value);
    }

    @Override
    public ByteBuf writeFloat(float value) {
        return buffer.writeFloat(value);
    }

    @Override
    public ByteBuf writeDouble(double value) {
        return buffer.writeDouble(value);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src) {
        return buffer.writeBytes(src);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src, int length) {
        return buffer.writeBytes(src, length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
        return buffer.writeBytes(src, srcIndex, length);
    }

    @Override
    public ByteBuf writeBytes(byte[] src) {
        return buffer.writeBytes(src);
    }

    @Override
    public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
        return buffer.writeBytes(src, srcIndex, length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuffer src) {
        return buffer.writeBytes(src);
    }

    @Override
    public int writeBytes(InputStream in, int length) throws IOException {
        return buffer.writeBytes(in, length);
    }

    @Override
    public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
        return buffer.writeBytes(in, length);
    }

    @Override
    public ByteBuf writeZero(int length) {
        return buffer.writeZero(length);
    }

    @Override
    public int indexOf(int fromIndex, int toIndex, byte value) {
        return buffer.indexOf(fromIndex, toIndex, value);
    }

    @Override
    public int bytesBefore(byte value) {
        return buffer.bytesBefore(value);
    }

    @Override
    public int bytesBefore(int length, byte value) {
        return buffer.bytesBefore(length, value);
    }

    @Override
    public int bytesBefore(int index, int length, byte value) {
        return buffer.bytesBefore(index, length, value);
    }

    @Override
    public int forEachByte(ByteBufProcessor processor) {
        return buffer.forEachByte(processor);
    }

    @Override
    public int forEachByte(int index, int length, ByteBufProcessor processor) {
        return buffer.forEachByte(index, length, processor);
    }

    @Override
    public int forEachByteDesc(ByteBufProcessor processor) {
        return buffer.forEachByteDesc(processor);
    }

    @Override
    public int forEachByteDesc(int index, int length, ByteBufProcessor processor) {
        return buffer.forEachByteDesc(index, length, processor);
    }

    @Override
    public ByteBuf copy() {
        return buffer.copy();
    }

    @Override
    public ByteBuf copy(int index, int length) {
        return buffer.copy(index, length);
    }

    @Override
    public ByteBuf slice() {
        return buffer.slice();
    }

    @Override
    public ByteBuf slice(int index, int length) {
        return buffer.slice(index, length);
    }

    @Override
    public ByteBuf duplicate() {
        return buffer.duplicate();
    }

    @Override
    public int nioBufferCount() {
        return buffer.nioBufferCount();
    }

    @Override
    public ByteBuffer nioBuffer() {
        return buffer.nioBuffer();
    }

    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        return buffer.nioBuffer(index, length);
    }

    @Override
    public ByteBuffer internalNioBuffer(int index, int length) {
        return buffer.internalNioBuffer(index, length);
    }

    @Override
    public ByteBuffer[] nioBuffers() {
        return buffer.nioBuffers();
    }

    @Override
    public ByteBuffer[] nioBuffers(int index, int length) {
        return buffer.nioBuffers(index, length);
    }

    @Override
    public boolean hasArray() {
        return buffer.hasArray();
    }

    @Override
    public byte[] array() {
        return buffer.array();
    }

    @Override
    public int arrayOffset() {
        return buffer.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return buffer.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return buffer.memoryAddress();
    }

    @Override
    public String toString(Charset charset) {
        return buffer.toString(charset);
    }

    @Override
    public String toString(int index, int length, Charset charset) {
        return buffer.toString(index, length, charset);
    }

    @Override
    public int hashCode() {
        return buffer.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return buffer.equals(obj);
    }

    @Override
    public int compareTo(ByteBuf buffer) {
        return this.buffer.compareTo(buffer);
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    @Override
    public ByteBuf retain(int increment) {
        return buffer.retain(increment);
    }

    @Override
    public ByteBuf retain() {
        return buffer.retain();
    }

    @Override
    public int refCnt() {
        return buffer.refCnt();
    }

    @Override
    public boolean release() {
        return buffer.release();
    }

    @Override
    public boolean release(int decrement) {
        return buffer.release(decrement);
    }
}
