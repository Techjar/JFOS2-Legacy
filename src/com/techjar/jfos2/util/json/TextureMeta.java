
package com.techjar.jfos2.util.json;

/**
 *
 * @author Techjar
 */
public class TextureMeta {
    public Animation animation;

    public class Animation {
        public int width;
        public int height;
        public float frametime;
        public Frame[] frames;

        public class Frame {
            public int index;
            public float time;
        }
    }
}
