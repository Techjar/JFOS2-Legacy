package com.techjar.jfos2.util;

import com.techjar.jfos2.util.logging.LogHelper;

/**
 *
 * @author Techjar
 */
public class ArgumentParser {
    public static void parse(String[] args, Argument... objects) {
        for (int i = 0; i < args.length; i++) {
            boolean found = false;
            for (Argument obj : objects) {
                if (obj.getName().equals(args[i].toLowerCase())) {
                    if (obj.getHasParameter()) {
                        obj.runAction(args[++i].toLowerCase());
                    } else obj.runAction(null);
                    found = true;
                    break;
                }
            }
            if (!found) LogHelper.warning("Unknown argument: %s", args[i]);
        }
    }

    public static abstract class Argument {
        private String name;
        private boolean hasParameter;

        public Argument(String name, boolean hasParameter) {
            this.name = name.toLowerCase();
            this.hasParameter = hasParameter;
        }

        public String getName() {
            return name;
        }

        public boolean getHasParameter() {
            return hasParameter;
        }

        public abstract void runAction(String paramater);
    }
}
