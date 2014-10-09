package com.techjar.jfos2.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;
import lombok.Cleanup;
import org.lwjgl.input.Controller;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

/**
 *
 * @author Techjar
 */
public final class Util {
    public static final Gson GSON = new GsonBuilder().create();

    private Util() {
    }

    public static boolean isValidCharacter(char ch) {
        return ch >= 32 && ch <= 126;
    }
    
    public static String stackTraceToString(Throwable throwable) {
        StringWriter stackTrace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTrace));
        return stackTrace.toString();
    }
    
    public static org.newdawn.slick.Color convertColor(org.lwjgl.util.Color color) {
        return new org.newdawn.slick.Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }
    
    public static org.lwjgl.util.Color convertColor(org.newdawn.slick.Color color) {
        return new org.lwjgl.util.Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static Vector2f convertVector2(Vector2 vector) {
        return new Vector2f(vector.getX(), vector.getY());
    }

    public static Vector2 convertVector2f(Vector2f vector) {
        return new Vector2(vector.getX(), vector.getY());
    }
    
    public static org.lwjgl.util.Color addColors(org.lwjgl.util.Color color1, org.lwjgl.util.Color color2) {
        return new org.lwjgl.util.Color(MathHelper.clamp(color1.getRed() + color2.getRed(), 0, 255), MathHelper.clamp(color1.getGreen() + color2.getGreen(), 0, 255), MathHelper.clamp(color1.getBlue() + color2.getBlue(), 0, 255));
    }

    public static org.lwjgl.util.Color subtractColors(org.lwjgl.util.Color color1, org.lwjgl.util.Color color2) {
        return new org.lwjgl.util.Color(MathHelper.clamp(color1.getRed() - color2.getRed(), 0, 255), MathHelper.clamp(color1.getGreen() - color2.getGreen(), 0, 255), MathHelper.clamp(color1.getBlue() - color2.getBlue(), 0, 255));
    }

    public static float getAxisValue(Controller con, String name) {
        if (name == null) return 0;
        for (int i = 0; i < con.getAxisCount(); i++) {
            if (name.equals(con.getAxisName(i))) return con.getAxisValue(i);
        }
        return 0;
    }

    /**
     * Will parse a valid IPv4/IPv6 address and port, may return garbage for invalid address formats. If no port was parsed it will be -1.
     */
    public static IPInfo parseIPAddress(String str) throws UnknownHostException {
        String ip;
        int port = -1;
        boolean ipv6 = false;
        if (str.indexOf(':') != -1) {
            if (str.indexOf('[') != -1 && str.indexOf(']') != -1) {
                ip = str.substring(1, str.indexOf(']'));
                port = Integer.parseInt(str.substring(str.indexOf(']') + 2));
                ipv6 = true;
            } else if (str.indexOf(':') == str.lastIndexOf(':')) {
                ip = str.substring(0, str.indexOf(':'));
                port = Integer.parseInt(str.substring(str.indexOf(':') + 1));
            } else ip = str;
        } else ip = str;
        return new IPInfo(InetAddress.getByName(ip), port, ipv6);
    }

    public static String getFileMD5(File file) throws IOException, NoSuchAlgorithmException {
        @Cleanup InputStream is = new FileInputStream(file);
        byte[] bytes = new byte[(int)file.length()];
        is.read(bytes);
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytes);
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public static String getFileMD5(String file) throws IOException, NoSuchAlgorithmException {
        return getFileMD5(new File(file));
    }

    /**
     * Compresses the byte array using deflate algorithm.
     */
    public static byte[] compresssBytes(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DeflaterOutputStream dos = new DeflaterOutputStream(out)) {
            dos.write(bytes);
        }
        return out.toByteArray();
    }

    /**
     * Decompresses the byte array using deflate algorithm.
     */
    public static byte[] decompresssBytes(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InflaterOutputStream dos = new InflaterOutputStream(out)) {
            dos.write(bytes);
        }
        return out.toByteArray();
    }

    public static long microTime() {
        return System.nanoTime() / 1000L;
    }

    public static long milliTime() {
        return System.nanoTime() / 1000000L;
    }

    public static Rectangle clipRectangle(Rectangle toClip, Rectangle clipTo) {
        if (!toClip.intersects(clipTo)) return new Rectangle(0, 0, 0, 0);
        float newX = MathHelper.clamp(toClip.getX(), clipTo.getX(), clipTo.getMaxX());
        float newY = MathHelper.clamp(toClip.getY(), clipTo.getY(), clipTo.getMaxY());
        float newWidth = MathHelper.clamp(toClip.getWidth(), 0, clipTo.getWidth() - (newX - clipTo.getX()));
        float newHeight = MathHelper.clamp(toClip.getHeight(), 0, clipTo.getHeight() - (newY - clipTo.getY()));
        return new Rectangle(newX, newY, newWidth, newHeight);
    }

    public static long bytesToMB(long bytes) {
        return bytes / 1048576;
    }

    public static String bytesToMBString(long bytes) {
        return bytesToMB(bytes) + " MB";
    }

    public static int getNextPowerOfTwo(int number) {
        int ret = Integer.highestOneBit(number);
        return ret < number ? ret << 1 : ret;
    }

    public static boolean isPowerOfTwo(int number) {
        return (number != 0) && (number & (number - 1)) == 0;
    }

    public static final class IPInfo {
        private InetAddress address;
        private int port;
        private boolean ipv6;

        private IPInfo(InetAddress address, int port, boolean ipv6) {
            this.address = address;
            this.port = port;
            this.ipv6 = ipv6;
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        public boolean isIPv6() {
            return ipv6;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IPInfo other = (IPInfo)obj;
            if (this.address != other.address && (this.address == null || !this.address.equals(other.address))) {
                return false;
            }
            if (this.port != other.port) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + (this.address != null ? this.address.hashCode() : 0);
            hash = 67 * hash + this.port;
            return hash;
        }

        @Override
        public String toString() {
            return port < 0 ? address.getHostAddress() : ipv6 ? '[' + address.getHostAddress() + "]:" + port : address.getHostAddress() + ':' + port;
        }
    }
}
