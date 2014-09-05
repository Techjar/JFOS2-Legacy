package com.techjar.jfos2.util;

/**
 *
 * @author Techjar
 */
public class ArgumentParser {
    

    public static class Argument {
        private String argument;
        private boolean hasParameter;
        private Runnable action;

        public Argument(String argument, boolean hasParameter, Runnable action) {
            this.argument = argument;
            this.hasParameter = hasParameter;
            this.action = action;
        }

        public String getArgument() {
            return argument;
        }

        public boolean hasParameter() {
            return hasParameter;
        }

        public void runAction() {
            action.run();
        }
    }
}
