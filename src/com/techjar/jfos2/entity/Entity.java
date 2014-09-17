/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.techjar.jfos2.entity;

import com.techjar.jfos2.util.Vector2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.lwjgl.input.Controller;

/**
 *
 * @author Techjar
 */
public abstract class Entity implements Comparable<Entity> {
    protected static int nextId;
    protected int id;
    protected Vector2 position = new Vector2();
    protected float angle;

    public Entity() {
        this(nextId++);
    }

    public Entity(int id) {
        this.id = id;
    }

    public abstract void update(double delta);
    public abstract void updateClient(double delta);
    public abstract void updateServer(double delta);
    public abstract void render();

    public void readData(DataInputStream stream) throws IOException {
        position.setX(stream.readFloat());
        position.setY(stream.readFloat());
        angle = stream.readFloat();
    }
    
    public void writeData(DataOutputStream stream) throws IOException {
        stream.writeFloat(position.getX());
        stream.writeFloat(position.getY());
        stream.writeFloat(angle);
    }

    public void readSpawnData(DataInputStream stream) throws IOException {
        readData(stream);
    }

    public void writeSpawnData(DataOutputStream stream) throws IOException {
        writeData(stream);
    }

    public boolean processKeyboardEvent() {
        return true;
    }

    public boolean processMouseEvent() {
        return true;
    }

    public boolean processControllerEvent(Controller controller) {
        return true;
    }

    public int getId() {
        return id;
    }

    public int getRenderPriority() {
        return 0;
    }

    public Vector2 getPosition() {
        return position.copy();
    }

    public void setPosition(Vector2 position) {
        this.position.set(position);
    }

    public void setPosition(float x, float y) {
        setPosition(new Vector2(x, y));
    }

    public float getX() {
        return position.getX();
    }

    public void setX(float x) {
        position.setX(x);
    }

    public float getY() {
        return position.getY();
    }

    public void setY(float y) {
        position.setY(y);
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    @Override
    public int compareTo(Entity other) {
        if (other == null) return 1;
        if (this.getRenderPriority() < other.getRenderPriority()) return -1;
        if (this.getRenderPriority() > other.getRenderPriority()) return 1;
        return 0;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + this.id;
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
        final Entity other = (Entity) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entity{" + "id=" + id + ", position=" + position + ", angle=" + angle + '}';
    }
}
