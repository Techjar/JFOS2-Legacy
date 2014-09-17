package com.techjar.jfos2.util;

/**
 * Class containing extra math-based commands which don't exist in java.lang.Math, such as clamp().
 * @author Techjar
 */
public final class MathHelper {
    private MathHelper() {
    }

    /**
     * Converts input number to a number within the specified range.
     * @param i input number
     * @param low minimum range
     * @param high maximum range
     * @return clamped number
     */
    public static int clamp(int i, int low, int high) {
        return Math.max(Math.min(i, high), low);
    }

    /**
     * Converts input number to a number within the specified range.
     * @param i input number
     * @param low minimum range
     * @param high maximum range
     * @return clamped number
     */
    public static long clamp(long i, long low, long high) {
        return Math.max(Math.min(i, high), low);
    }

    /**
     * Converts input number to a number within the specified range.
     * @param i input number
     * @param low minimum range
     * @param high maximum range
     * @return clamped number
     */
    public static double clamp(double i, double low, double high) {
        return Math.max(Math.min(i, high), low);
    }

    /**
     * Converts input number to a number within the specified range.
     * @param i input number
     * @param low minimum range
     * @param high maximum range
     * @return clamped number
     */
    public static float clamp(float i, float low, float high) {
        return Math.max(Math.min(i, high), low);
    }

    /**
     * Returns a number indicating the sign (+/-) of a number, as either -1, 0, or 1.
     * @param i input number
     * @return sign of number
     */
    public static int sign(int i) {
        return clamp(i, -1, 1);
    }

    /**
     * Returns a number indicating the sign (+/-) of a number, as either -1, 0, or 1.
     * @param i input number
     * @return sign of number
     */
    public static long sign(long i) {
        return clamp(i, -1, 1);
    }

    /**
     * Returns a number indicating the sign (+/-) of a number, as either -1, 0, or 1.
     * @param i input number
     * @return sign of number
     */
    public static double sign(double i) {
        return clamp(i < 0 ? Math.floor(i) : Math.ceil(i), -1, 1);
    }

    /**
     * Returns a number indicating the sign (+/-) of a number, as either -1, 0, or 1.
     * @param i input number
     * @return sign of number
     */
    public static float sign(float i) {
        return clamp(i < 0 ? (float)Math.floor(i) : (float)Math.ceil(i), -1, 1);
    }
}
