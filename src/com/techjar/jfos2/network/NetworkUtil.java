
package com.techjar.jfos2.network;

import java.io.IOException;
import lombok.SneakyThrows;

/**
 *
 * @author Techjar
 */
public final class NetworkUtil {
    private NetworkUtil() {
    }

    public static void marshalObject(Object obj, PacketBuffer buffer) throws IOException {
        if (obj instanceof Byte) {
            buffer.writeByte((Byte)obj);
        } else if (obj instanceof Short) {
            buffer.writeShort((Short)obj);
        } else if (obj instanceof Integer) {
            buffer.writeInt((Integer)obj);
        } else if (obj instanceof Long) {
            buffer.writeLong((Long)obj);
        } else if (obj instanceof Float) {
            buffer.writeFloat((Float)obj);
        } else if (obj instanceof Double) {
            buffer.writeDouble((Double)obj);
        } else if (obj instanceof Boolean) {
            buffer.writeBoolean((Boolean)obj);
        } else if (obj instanceof Character) {
            buffer.writeChar((Character)obj);
        } else if (obj instanceof String) {
            buffer.writeString((String)obj);
        } else if (obj instanceof byte[]) {
            buffer.writeVarInt(((byte[])obj).length);
            buffer.writeBytes((byte[])obj);
        } else if (obj instanceof Marshallable) {
            ((Marshallable)obj).writeData(buffer);
        } else {
            throw new IllegalArgumentException(obj.getClass().getName() + " is not marshallable!");
        }
    }

    @SneakyThrows(Exception.class)
    public static Object unmarshalObject(Class type, PacketBuffer buffer) throws IOException {
        if (type == byte.class || type == Byte.class) {
            return buffer.readByte();
        } else if (type == short.class || type == Short.class) {
            return buffer.readShort();
        } else if (type == int.class || type == Integer.class) {
            return buffer.readInt();
        } else if (type == long.class || type == Long.class) {
            return buffer.readLong();
        } else if (type == float.class || type == Float.class) {
            return buffer.readFloat();
        } else if (type == double.class || type == Double.class) {
            return buffer.readDouble();
        } else if (type == boolean.class || type == Boolean.class) {
            return buffer.readBoolean();
        } else if (type == char.class || type == Character.class) {
            return buffer.readChar();
        } else if (type == String.class) {
            return buffer.readString();
        } else if (type == byte[].class) {
            return buffer.readBytes(buffer.readVarInt()).array();
        } else if (Marshallable.class.isAssignableFrom(type)) {
            Object obj = type.newInstance();
            ((Marshallable)obj).readData(buffer);
            return obj;
        } else {
            throw new IllegalArgumentException(type.getName() + " is not marshallable!");
        }
    }
}
