package com.techjar.jfos2.util;

import com.techjar.jfos2.MathHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;

/**
 *
 * @author Techjar
 */
public class AsteroidGenerator {
    private static final Random random = new Random();
    private static final ExponentialRandom expRandom = new ExponentialRandom(1.1D, random);

    public static float[] generatePoints(int baseRadius, int maxDeviation, int maxDifference, int minIncrement, int maxIncrement) {
        List<Float> points = new ArrayList<>();
        int deviation = 0;
        for (int deg = 0; deg < 360 - maxIncrement / 2; deg += minIncrement + random.nextInt(maxIncrement - minIncrement)) {
            double rad = Math.toRadians(deg);
            int difference = random.nextInt(maxDifference * 2) - maxDifference;
            deviation = MathHelper.clamp(difference + deviation, -maxDeviation, maxDeviation);
            double x = Math.sin(rad) * (baseRadius + deviation);
            double y = Math.cos(rad) * (baseRadius + deviation);
            points.add((float)x);
            points.add((float)y);
        }

        float[] array = new float[points.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = points.get(i);
        }
        return array;
    }

    public static Circle[] generateCraters(Shape shape, int baseRadius, int maxDeviation, int attempts) {
        shape.setCenterX(0); shape.setCenterY(0);
        List<Circle> shapes = new ArrayList<>();
        float radius = shape.getBoundingCircleRadius();
        outer: for (int i = 0; i < attempts; i++) {
            float randX = random.nextInt((int)radius * 2) - radius;
            float randY = random.nextInt((int)radius * 2) - radius;
            Circle crater = new Circle(shape.getCenterX() + randX, shape.getCenterY() + randY, baseRadius + (random.nextInt(maxDeviation * 2) - maxDeviation));
            if (!shape.contains(crater)) continue;
            for (Shape other : shapes)
                if (crater.intersects(other)) continue outer;
            shapes.add(crater);
        }
        return shapes.toArray(new Circle[shapes.size()]);
    }

    public static Asteroid generate(double baseMult, double minMult, double maxMult) {
        double mult = MathHelper.clamp(expRandom.nextDouble() * baseMult, minMult, maxMult);
        Polygon shape = new Polygon(AsteroidGenerator.generatePoints((int)(30 * mult), (int)(10 * mult), (int)(5 * mult), 10, 30));
        return new Asteroid(shape, AsteroidGenerator.generateCraters(shape, (int)(10 * mult), (int)(5 * mult), (int)(200 * mult)));
    }

    public static Asteroid generate() {
        return generate(1.0D, 0.8D, 5.0D);
    }
}
