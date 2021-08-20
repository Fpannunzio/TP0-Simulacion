package ar.edu.itba.simulacion.particle;

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
        if (args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        final ObjectMapper mapper = new ObjectMapper();
        final ParticleGenerationConfig config = mapper.readValue(Path.of(args[0]).toFile(),
                ParticleGenerationConfig.class);
        mapper.writeValue(Path.of(config.outputFile).toFile(), particleGenerator(config));

    }

    public static List<Particle2D> particleGenerator(ParticleGenerationConfig config) {

        final double minRadius = Math.max(MIN_RADIUS, config.minRadius);

        final List<Particle2D> ret = new ArrayList<>(config.particleCount);

        int tries = 0;
        int particleCount = 0;
        while (particleCount < config.particleCount && tries < MAX_FAILURE_TOLERANCE) {
            final Particle2D particle = Particle2D.randomParticle(particleCount, config.L, config.velocity, minRadius, config.maxRadius);
            if (!particle.collides(ret, config.L, config.periodicOutline)) {
                ret.add(particle);
                particleCount++;
            }
            tries++;
        }

        return ret;
    }

    public static class ParticleGenerationConfig {
        public int particleCount;
        public double L;
        public double velocity;
        public boolean periodicOutline;
        public double minRadius;
        public double maxRadius;
        public String outputFile;


        public ParticleGenerationConfig() {
            //deserialization
        }

        public ParticleGenerationConfig(int particleCount, double L, double velocity, boolean periodicOutline, double minRadius,
                double maxRadius, String outputFile) {
            this.particleCount = particleCount;
            this.L = L;
            this.velocity = velocity;
            this.periodicOutline = periodicOutline;
            this.minRadius = minRadius;
            this.maxRadius = maxRadius;
            this.outputFile = outputFile;
        }
    }
}
