
package com.techjar.jfos2.util;

/**
 *
 * @author Techjar
 */
public class Vector2 {
    public static final Vector2 ZERO = new Vector2();
    public static final Vector2 ONE = new Vector2(1, 1);

    protected float x;
    protected float y;

    public Vector2() {
    }

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2(Vector2 other) {
        this.x = other.x;
        this.y = other.y;
    }

    public Vector2 copy() {
        return new Vector2(this);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vector2 other) {
        x = other.x;
        y = other.y;
    }

    public Vector2 add(Vector2 other) {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    public Vector2 subtract(Vector2 other) {
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    public Vector2 multiply(float number) {
        return new Vector2(this.x * number, this.y * number);
    }

    public Vector2 divide(float number) {
        return new Vector2(this.x / number, this.y / number);
    }

    public Vector2 negate() {
        return new Vector2(-this.x, -this.y);
    }

    public float angle(Vector2 other) {
        return (float)Math.toDegrees(Math.atan2(other.y - y, other.x - x));
    }

    public float length() {
        return (float)Math.sqrt(x * x + y * y);
    }

    public float lengthSquared() {
        return x * x + y * y;
    }

    public float distance(Vector2 other) {
        return (float)Math.sqrt((other.x - x) * (other.x - x) + (other.y - y) * (other.y - y));
    }

    public float distanceSquared(Vector2 other) {
        return (other.x - x) * (other.x - x) + (other.y - y) * (other.y - y);
    }

    public Vector2 normalize() {
        return divide(length());
    }

    public float dot(Vector2 other) {
        return x * other.x + y * other.y;
    }

    public static Vector2 direction(float angle) {
        angle = (float)Math.toRadians(angle);
        return new Vector2((float)Math.cos(angle), (float)Math.sin(angle));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Float.floatToIntBits(this.x);
        hash = 37 * hash + Float.floatToIntBits(this.y);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vector2 other = (Vector2) obj;
        if (Float.floatToIntBits(this.x) != Float.floatToIntBits(other.x)) {
            return false;
        }
        if (Float.floatToIntBits(this.y) != Float.floatToIntBits(other.y)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Vector2{" + "x=" + x + ", y=" + y + '}';
    }
}
