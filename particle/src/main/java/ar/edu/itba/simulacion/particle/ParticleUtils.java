package ar.edu.itba.simulacion.particle;

import java.util.Random;

public final class ParticleUtils {
    private ParticleUtils() {
        // static
    }

    public static double randDouble(final Random randomGen, final double min, final double max) {
        return min + randomGen.nextDouble() * (max - min);
    }
}
