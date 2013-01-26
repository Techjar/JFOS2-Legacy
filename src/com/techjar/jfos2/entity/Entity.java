/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.techjar.jfos2.entity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.lwjgl.input.Controller;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Techjar
 */
public abstract class Entity {
    protected static int nextId;
    protected int id;
    protected Vector2f position = new Vector2f();
    protected float angle;

    public Entity() {
        this(nextId++);
    }

    public Entity(int id) {
        this.id = id;
    }

    public abstract void update();
    public abstract void updateClient();
    public abstract void updateServer();
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

    public Vector2f getPosition() {
        return new Vector2f(position);
    }

    public void setPosition(Vector2f position) {
        this.position.set(position);
    }

    public void setPosition(float x, float y) {
        setPosition(new Vector2f(x, y));
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
}
