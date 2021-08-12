package ar.edu.itba.simulacion.tp0;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
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

        final ParticleGenerationConfig config = mapper.readValue(Path.of(args[0]).toFile(), ParticleGenerationConfig.class);

        final double minRadius = Math.max(MIN_RADIUS, config.minRadius);

        final List<Particle> ret = new ArrayList<>(config.particleCount);

        int tries = 0;
        int particleCount = 0;
        while(particleCount < config.particleCount && tries < MAX_FAILURE_TOLERANCE) {
            final Particle particle = Particle.randomParticle(particleCount, config.L, minRadius, config.maxRadius);
            if(!particle.collides(ret)) {
                ret.add(particle);
                particleCount++;
            }
            tries++;
        }

        mapper.writeValue(Path.of(config.outputFile).toFile(), ret);
    }

    private static class ParticleGenerationConfig {
        public int          particleCount;
        public double       L;
        public double       minRadius;
        public double       maxRadius;
        public String       outputFile;
    }
}
