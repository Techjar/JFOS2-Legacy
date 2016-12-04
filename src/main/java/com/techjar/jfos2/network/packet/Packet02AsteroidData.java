
package com.techjar.jfos2.network.packet;

import com.techjar.jfos2.network.ConnectionState;
import com.techjar.jfos2.network.Packet;
import com.techjar.jfos2.network.PacketBuffer;
import com.techjar.jfos2.network.handler.NetHandler;
import com.techjar.jfos2.util.Asteroid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Polygon;

/**
 * @author Techjar
 */
public class Packet02AsteroidData extends Packet {
	protected List<Asteroid> asteroids;

	public Packet02AsteroidData() {
	}

	public Packet02AsteroidData(List<Asteroid> asteroids) {
		this.asteroids = asteroids;
	}

	@Override
	@SneakyThrows(IOException.class)
	public void readData(PacketBuffer buffer) {
		asteroids = new ArrayList<>();
		while (buffer.isReadable()) {
			float colorMult = buffer.readFloat();
			float[] points = new float[buffer.readVarInt()];
			for (int i = 0; i < points.length; i++) {
				points[i] = buffer.readFloat();
			}
			Circle[] craters = new Circle[buffer.readVarInt()];
			for (int i = 0; i < craters.length; i++) {
				craters[i] = new Circle(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
			}
			Polygon body = new Polygon(points);
			body.setCenterX(0);
			body.setCenterY(0);
			asteroids.add(new Asteroid(body, craters, colorMult));
		}
	}

	@Override
	public void writeData(PacketBuffer buffer) {
		for (Asteroid asteroid : asteroids) {
			buffer.writeFloat(asteroid.getColorMult());
			float[] points = asteroid.getBody().getPoints();
			Circle[] craters = asteroid.getCraters();
			buffer.writeVarInt(points.length);
			for (int i = 0; i < points.length; i++) {
				buffer.writeFloat(points[i]);
			}
			buffer.writeVarInt(craters.length);
			for (int i = 0; i < craters.length; i++) {
				Circle crater = craters[i];
				buffer.writeFloat(crater.getCenterX());
				buffer.writeFloat(crater.getCenterY());
				buffer.writeFloat(crater.getRadius());
			}
		}
	}

	@Override
	public void processClient(NetHandler handler) {
		// TODO
	}

	@Override
	public void processServer(NetHandler handler) {
		// TODO
	}

	@Override
	public ConnectionState getConnectionState() {
		return ConnectionState.HANDSHAKE;
	}
}
