/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.techjar.jfos2.entity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.techjar.jfos2.network.watcher.FieldWatcher;
import com.techjar.jfos2.network.watcher.Watchable;
import com.techjar.jfos2.network.watcher.Watcher;
import com.techjar.jfos2.player.Player;
import com.techjar.jfos2.server.Server;
import com.techjar.jfos2.util.Vector2;
import com.techjar.jfos2.world.World;

import java.lang.reflect.Modifier;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.lwjgl.input.Controller;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;

/**
 * @author Techjar
 */
public abstract class Entity implements Watchable, Comparable<Entity> {
	private static final BiMap<Integer, Class<? extends Entity>> entityMap = HashBiMap.create();
	protected final int id;
	protected World world;
	protected World worldChange;
	protected Vector2 position = new Vector2();
	protected Vector2 velocity = new Vector2();
	protected float angle;
	protected float angularVelocity;
	protected boolean dead;
	protected Shape boundingBox = new Point(0, 0);
	protected Shape lastBoundingBox = null;
	Vector2 lastPosition = position;
	float lastAngle = angle;
	protected FieldWatcher watcher;

	public Entity() {
		this(Server.getNextObjectId());
	}

	public Entity(int id) {
		this.id = id;
		watcher = new FieldWatcher(this, id);
		watcher.watchField(Entity.class, 0, "position", false);
		watcher.watchField(Entity.class, 1, "velocity", true);
		watcher.watchField(Entity.class, 2, "angle", false);
		watcher.watchField(Entity.class, 3, "angularVelocity", true);
		watcher.watchField(Entity.class, 4, "dead", true);
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
		if (Modifier.isAbstract(clazz.getModifiers()))
			throw new IllegalArgumentException("Cannot register abstract entity class: " + clazz.getName());
		if (entityMap.containsKey(type)) throw new RuntimeException("Entity type already in use: " + type);
		if (entityMap.containsValue(clazz))
			throw new RuntimeException("Entity class already registered: " + clazz.getName());
		entityMap.put(type, clazz);
	}

	public void update(float delta) {
		lastPosition = position;
		lastAngle = angle;
		position = position.add(velocity.multiply(delta));
		angle = (angle + angularVelocity * delta) % 360;
	}

	public void updateClient(float delta) {
	}

	public void updateServer(float delta) {
	}

	public void updateBoundingBox() {
		if (!position.equals(lastPosition)) {
			Vector2 posOffset = position.subtract(lastPosition);
			boundingBox = boundingBox.transform(Transform.createTranslateTransform(posOffset.getX(), posOffset.getY()));
		}
		if (angle != lastAngle) {
			boundingBox = boundingBox.transform(Transform.createRotateTransform((float) Math.toRadians(angle - lastAngle), position.getX(), position.getY()));
		}
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
		return boundingBox;
	}

	public final int getEntityType() {
		return entityMap.inverse().get(this.getClass());
	}

	@Override
	public final int getId() {
		return id;
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

	/**
	 * Gets world the entity is changing to.
	 */
	public World getWorldChange() {
		return worldChange;
	}

	/**
	 * Marks this entity to change to the specified world next tick. If world is not null, it will be treated as {@link #setDead dead} for the remainder of the current tick.
	 * Has no effect if this entity is already dead for another reason.
	 */
	public void setWorldChange(World world) {
		if (dead && worldChange == null) return;
		worldChange = world;
		dead = world != null;
	}

	public Vector2 getPosition() {
		return position.copy();
	}

	public void setPosition(@NonNull Vector2 position) {
		Vector2 lastPos = this.position.copy();
		lastPosition = position.copy();
		this.position.set(position);
		if (!lastPos.equals(this.position)) {
			watcher.forceSyncField("position");
			Vector2 posOffset = this.position.subtract(lastPos);
			boundingBox = boundingBox.transform(Transform.createTranslateTransform(posOffset.getX(), posOffset.getY()));
		}
	}

	public void setPosition(float x, float y) {
		setPosition(new Vector2(x, y));
	}

	public float getX() {
		return position.getX();
	}

	public void setX(float x) {
		setPosition(x, position.getY());
	}

	public float getY() {
		return position.getY();
	}

	public void setY(float y) {
		setPosition(position.getX(), y);
	}

	public Vector2 getVelocity() {
		return velocity.copy();
	}

	public void setVelocity(@NonNull Vector2 velocity) {
		this.velocity.set(velocity);
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

	/**
	 * Gets the angle in degrees.
	 */
	public float getAngle() {
		return angle;
	}

	/**
	 * Sets the angle in degrees.
	 */
	public void setAngle(float angle) {
		float lastAng = this.angle;
		lastAngle = angle;
		this.angle = angle % 360;
		if (this.angle != lastAng) {
			watcher.forceSyncField("angle");
			boundingBox = boundingBox.transform(Transform.createRotateTransform(this.angle - lastAng, position.getX(), position.getY()));
		}
	}

	/**
	 * Gets the angular velocity in degrees per second.
	 */
	public float getAngularVelocity() {
		return angularVelocity;
	}

	/**
	 * Sets the angular velocity in degrees per second.
	 */
	public void setAngularVelocity(float angularVelocity) {
		this.angularVelocity = angularVelocity;
	}

	/**
	 * See {@link #setDead()}
	 */
	public boolean isDead() {
		return dead;
	}

	/**
	 * Marks this entity for removal next tick.
	 */
	public void setDead() {
		dead = true;
	}

	/**
	 * Should this entity entirely skip collision checks?
	 */
	public boolean isInert() {
		return false;
	}

	/**
	 * Can this entity collide with the passed entity?
	 */
	public boolean canCollide(Entity other) {
		return true;
	}

	/**
	 * Can this entity collide with the passed entity client-side?
	 */
	public boolean canCollideClient(Entity other) {
		return false;
	}

	/**
	 * Called to process collision.
	 */
	public void onCollide(Entity other) {
	}

	@Override
	public Watcher getWatcher() {
		return watcher;
	}

	@Override
	public Player[] getExcludedPlayers() {
		return new Player[0];
	}

	@Override
	public int compareTo(Entity other) {
		if (other == null) return 1;
		if (this.getRenderPriority() < other.getRenderPriority()) return -1;
		if (this.getRenderPriority() > other.getRenderPriority()) return 1;
		return 0;
	}
}
