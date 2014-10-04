/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.techjar.jfos2.entity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.techjar.jfos2.network.NetworkedObject;
import com.techjar.jfos2.network.PacketBuffer;
import com.techjar.jfos2.player.Player;
import com.techjar.jfos2.util.ObjectType;
import com.techjar.jfos2.util.Vector2;
import com.techjar.jfos2.world.World;
import java.lang.reflect.Modifier;
import lombok.SneakyThrows;
import org.lwjgl.input.Controller;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Shape;

/**
 *
 * @author Techjar
 */
public abstract class Entity implements NetworkedObject, Comparable<Entity> {
    private static int nextId;
    private static final BiMap<Integer, Class<? extends Entity>> entityMap = HashBiMap.create();
    protected final int id;
    protected World world;
    protected World worldChange;
    protected Vector2 position = new Vector2();
    protected Vector2 velocity = new Vector2();
    protected float angle;
    protected float angularVelocity;
    protected boolean dead;
    protected boolean needsSync;

    public Entity() {
        this(nextId++);
    }

    public Entity(int id) {
        this.id = id;
    }

    @SneakyThrows(Exception.class)
    public static Entity generateEntity(int type) {
        Class<? extends Entity> clazz = entityMap.get(type);
        return clazz == null ? null : clazz.newInstance();
    }

    @SneakyThrows(Exception.class)
    public static Entity generateEntity(int type, int id) {
        Class<? extends Entity> clazz = entityMap.get(type);
        return clazz == null ? null : clazz.getConstructor(int.class).newInstance(id);
    }

    public static void registerEntity(int type, Class<? extends Entity> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers())) throw new IllegalArgumentException("Cannot register abstract entity class: " + clazz.getName());
        if (entityMap.containsKey(type)) throw new RuntimeException("Entity type already in use: " + type);
        if (entityMap.inverse().containsKey(clazz)) throw new RuntimeException("Entity class already registered: " + clazz.getName());
        entityMap.put(type, clazz);
    }

    public void update(float delta) {
        position = position.add(velocity.multiply(delta));
        angle += angularVelocity * delta;
    }

    public void updateClient(float delta) {
    }

    public void updateServer(float delta) {
    }

    public void render() {
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

    public Shape getBoundingBox() {
        return new Point(position.getX(), position.getY());
    }

    public final int getEntityType() {
        return entityMap.inverse().get(this.getClass());
    }

    public int getRenderPriority() {
        return 0;
    }

    public World getWorld() {
        return world;
    }

    /**
     * Sets the world that this entity is in. Do not call this to change the entity's world, use {@link #setWorldChange} instead!
     *
     * @param world world to be set
     */
    public void setWorld(World world) {
        this.world = world;
    }

    public World getWorldChange() {
        return worldChange;
    }

    public void setWorldChange(World world) {
        this.worldChange = world;
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

    public Vector2 getVelocity() {
        return velocity.copy();
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity.set(velocity);
        needsSync = true;
    }

    public void setVelocity(float x, float y) {
        setVelocity(new Vector2(x, y));
    }

    public float getVelocityX() {
        return velocity.getX();
    }

    public void setVelocityX(float x) {
        velocity.setX(x);
    }

    public float getVelocityY() {
        return velocity.getY();
    }

    public void setVelocityY(float y) {
        velocity.setY(y);
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
        needsSync = true;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead() {
        dead = true;
    }

    public boolean isInert() {
        return false;
    }

    public boolean canCollide(Entity other) {
        return true;
    }

    public void onCollide(Entity other) {
    }

    @Override
    public void readSyncData(PacketBuffer buffer) {
        position.setX(buffer.readFloat());
        position.setY(buffer.readFloat());
        velocity.setX(buffer.readFloat());
        velocity.setY(buffer.readFloat());
        angle = buffer.readFloat();
        angularVelocity = buffer.readFloat();
    }

    @Override
    public void writeSyncData(PacketBuffer buffer) {
        buffer.writeFloat(position.getX());
        buffer.writeFloat(position.getY());
        buffer.writeFloat(velocity.getX());
        buffer.writeFloat(velocity.getY());
        buffer.writeFloat(angle);
        buffer.writeFloat(angularVelocity);
    }

    @Override
    public boolean isSynced() {
        return !needsSync;
    }

    @Override
    public void markSynced() {
        needsSync = false;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.ENTITY;
    }

    @Override
    public boolean shouldSendToPlayer(Player player) {
        return true;
    }

    @Override
    public int compareTo(Entity other) {
        if (other == null) return 1;
        if (this.getRenderPriority() < other.getRenderPriority()) return -1;
        if (this.getRenderPriority() > other.getRenderPriority()) return 1;
        return 0;
    }
}
