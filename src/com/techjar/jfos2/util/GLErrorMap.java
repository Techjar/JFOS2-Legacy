
package com.techjar.jfos2.util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Techjar
 */
public class GLErrorMap {
    private static final Map<Integer, String> map = new HashMap<>();

    static {
        map.put(0x0500, "GL_INVALID_ENUM​");
        map.put(0x0501, "GL_INVALID_VALUE​");
        map.put(0x0502, "GL_INVALID_OPERATION");
        map.put(0x0503, "GL_STACK_OVERFLOW​");
        map.put(0x0504, "GL_STACK_UNDERFLOW​");
        map.put(0x0505, "GL_OUT_OF_MEMORY");
        map.put(0x0506, "GL_INVALID_FRAMEBUFFER_OPERATION");
        map.put(0x8031, "GL_TABLE_TOO_LARGE");
    }

    public static String getName(int error) {
        if (map.containsKey(error)) return map.get(error);
        return Integer.toString(error);
    }
}
