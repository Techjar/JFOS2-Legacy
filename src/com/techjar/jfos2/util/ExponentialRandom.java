
package com.techjar.jfos2.util;

import java.util.Random;

/**
 *
 * @author Techjar
 */
public class ExponentialRandom {
    private final double lambda;
    private final Random random;

    public ExponentialRandom(double lambda, Random random) {
        this.lambda = lambda;
        this.random = random;
    }

    public ExponentialRandom(double lambda, int seed) {
        this(lambda, new Random(seed));
    }

    public ExponentialRandom(double lambda) {
        this(lambda, new Random());
    }

    public double nextDouble() {
        return -(Math.log(random.nextDouble()) / lambda);
    }
}
