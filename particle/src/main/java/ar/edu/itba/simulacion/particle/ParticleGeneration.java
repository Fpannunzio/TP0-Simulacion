package ar.edu.itba.simulacion.particle;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ParticleGeneration {

    private ParticleGeneration() {
        // Static class
    }

    private static final double MIN_RADIUS = 0.1;
    private static final int MAX_FAILURE_TOLERANCE = 10_000;

    public static void main(final String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        final ObjectMapper mapper = new ObjectMapper();

        final ParticleGenerationConfig config = mapper.readValue(new File(args[0]), ParticleGenerationConfig.class);

        mapper.writeValue(new File(config.outputFile), particleGenerator(config));
    }

    public static List<Particle2D> particleGenerator(final ParticleGenerationConfig config) {

        final double minRadius = Math.max(MIN_RADIUS, config.minRadius);

        final List<Particle2D> ret = new ArrayList<>(config.particleCount);

        // TODO(tobi): Cambiar brute force por CellIndexMethod
        int tries = 0;
        int particleCount = 0;
        while(particleCount < config.particleCount && tries < MAX_FAILURE_TOLERANCE) {
            final Particle2D particle = Particle2D.randomParticle(particleCount, config.spaceWidth, config.minVelocity, config.maxVelocity, minRadius, config.maxRadius);
            if(particle.getRadius() == 0 || !particle.collides(ret, config.spaceWidth, config.periodicBorder)) {
                ret.add(particle);
                particleCount++;
            }
            tries++;
        }

        return ret;
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class ParticleGenerationConfig {
        public int     particleCount;
        public double  spaceWidth;
        public boolean periodicBorder;
        public double  minVelocity;
        public double  maxVelocity;
        public double  minRadius;
        public double  maxRadius;
        public String  outputFile;
    }
}
