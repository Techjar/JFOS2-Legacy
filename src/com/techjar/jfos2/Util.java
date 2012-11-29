/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.techjar.jfos2;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author Techjar
 */
public final class Util {
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
    
    public static org.lwjgl.util.Color addColors(org.lwjgl.util.Color color1, org.lwjgl.util.Color color2) {
        return new org.lwjgl.util.Color(MathHelper.clamp(color1.getRed() + color2.getRed(), 0, 255), MathHelper.clamp(color1.getGreen() + color2.getGreen(), 0, 255), MathHelper.clamp(color1.getBlue() + color2.getBlue(), 0, 255));
    }

    public static IPInfo parseIPAddress(String str) throws UnknownHostException {
        String ip; int port = 0; boolean ipv6 = false;
        if (str.indexOf(":") != -1) {
            if (str.indexOf("[") != -1 && str.indexOf("]") != -1) {
                ip = str.substring(1, str.indexOf("]"));
                port = Integer.parseInt(str.substring(str.indexOf("]") + 2));
                ipv6 = true;
            }
            else if (str.indexOf(":") == str.lastIndexOf(":")) {
                ip = str.substring(0, str.indexOf(":"));
                port = Integer.parseInt(str.substring(str.indexOf(":") + 1));
            }
            else ip = str;
        }
        else ip = str;

        return new IPInfo(InetAddress.getByName(ip), port, ipv6);
    }

    public static final class IPInfo {
        private InetAddress ip;
        private int port;
        private boolean ipv6;

        public IPInfo(InetAddress ip, int port, boolean ipv6) {
            this.ip = ip;
            this.port = port;
            this.ipv6 = ipv6;
        }

        public InetAddress getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }

        public boolean isIPv6() {
            return ipv6;
        }

        @Override
        public String toString() {
            return ipv6 ? "[" + ip.getHostAddress() + "]:" + port : ip.getHostAddress() + ":" + port;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IPInfo other = (IPInfo) obj;
            if (this.ip != other.ip && (this.ip == null || !this.ip.equals(other.ip))) {
                return false;
            }
            if (this.port != other.port) {
                return false;
            }
            if (this.ipv6 != other.ipv6) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + (this.ip != null ? this.ip.hashCode() : 0);
            hash = 67 * hash + this.port;
            hash = 67 * hash + (this.ipv6 ? 1 : 0);
            return hash;
        }
    }
}
