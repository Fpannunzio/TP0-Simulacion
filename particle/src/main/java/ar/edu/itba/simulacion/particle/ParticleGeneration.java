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

    private static final double MIN_RADIUS = 0;
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
        return generateParticles(
            config.particleCount, config.spaceWidth, config.periodicBorder, config.minVelocity, config.maxVelocity,
            config.minRadius, config.maxRadius
        );
    }

    public static List<Particle2D> generateParticles(
        final int particleCount, final double spaceWidth, final boolean periodicBorder, final double minVelocity,
        final double maxVelocity, final double minRadius, final double maxRadius
    ) {
        final List<Particle2D> ret = new ArrayList<>(particleCount);

        generateAdditionalParticles(
            ret, particleCount, spaceWidth, periodicBorder, minVelocity, maxVelocity, minRadius, maxRadius
        );

        return ret;
    }

    public static void generateAdditionalParticles(
        final List<Particle2D> existingParticles, final int targetParticleCount, final double spaceWidth,
        final boolean periodicBorder, final double minVelocity, final double maxVelocity, final double minRadius,
        final double maxRadius
    ) {
        final double realMinRadius = Math.max(MIN_RADIUS, minRadius);

        // TODO(tobi): Cambiar brute force por CellIndexMethod
        int tries = 0;
        int particleCount = existingParticles.size();
        while(particleCount < targetParticleCount && tries < MAX_FAILURE_TOLERANCE) {
            final Particle2D particle = Particle2D.randomParticle(particleCount, spaceWidth, minVelocity, maxVelocity, realMinRadius, maxRadius);
            if(particle.getRadius() == 0 || !particle.collides(existingParticles, spaceWidth, periodicBorder)) {
                existingParticles.add(particle);
                particleCount++;
            }
            tries++;
        }
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
        public double  minMass;
        public double  maxMass;
        public String  outputFile;
    }

    
}
