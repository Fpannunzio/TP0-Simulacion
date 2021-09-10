package ar.edu.itba.simulacion.particle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import ar.edu.itba.simulacion.particle.ParticleGeneration.ParticleGenerationConfig;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public class BrownianParticleGeneration {
  
    private static final double MIN_RADIUS = 0;
    private static final double MIN_MASS = 0;
    private static final int MAX_FAILURE_TOLERANCE = 10_000;
    
    public static void main(final String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        final ObjectMapper mapper = new ObjectMapper();

        final BrownianParticleGenerationConfig config = mapper.readValue(new File(args[0]), BrownianParticleGenerationConfig.class);

        mapper.writeValue(new File(config.littleParticles.outputFile), particleGenerator(config));
    }

    public static List<Particle2D> particleGenerator(final BrownianParticleGenerationConfig config) {
        return generateParticles(
            config.littleParticles.particleCount, config.littleParticles.spaceWidth, config.littleParticles.periodicBorder, 
            config.littleParticles.minVelocity, config.littleParticles.maxVelocity, config.littleParticles.minRadius,
            config.littleParticles.maxRadius, config.littleParticles.minMass, config.littleParticles.maxMass,
            config.bigParticleRadius, config.bigParticleMass, config.bigParticleVelocity,
            config.bigParticleXPosition, config.bigParticleYPosition
        );
    }

    public static List<Particle2D> generateParticles(
        final int particleCount, final double spaceWidth, final boolean periodicBorder, final double minVelocity,
        final double maxVelocity, final double minRadius, final double maxRadius, final double minMass,
        final double maxMass, final double bigParticleRadius, final double bigParticleMass,
        final double bigParticleVelocity, final double bigParticleXPosition, final double bigParticleYPosition
    ) {
        final List<Particle2D> ret = new ArrayList<>(particleCount);

        generateBigParticle(ret, bigParticleRadius, bigParticleMass, bigParticleVelocity, bigParticleVelocity, bigParticleXPosition, bigParticleYPosition);

        generateAdditionalParticles(
            ret, particleCount, spaceWidth, periodicBorder, minVelocity, maxVelocity, minRadius, maxRadius, minMass, maxMass
        );

        return ret;
    }

    public static void generateBigParticle(final List<Particle2D> existingParticles, final double radius, final double mass, final double velocityX, final double velocityY, final double x, final double y) {
        existingParticles.add(
        Particle2D.builder()
        .withId(0)
        .withRadius(radius)
        .withVelocityX(velocityX)
        .withVelocityY(velocityY)
        .withX(x)
        .withY(y)
        .build()
        );
    }

    public static void generateAdditionalParticles(
        final List<Particle2D> existingParticles, final int targetParticleCount, final double spaceWidth,
        final boolean periodicBorder, final double minVelocity, final double maxVelocity, final double minRadius,
        final double maxRadius, final double minMass, final double maxMass
    ) {
        final double realMinRadius = Math.max(MIN_RADIUS, minRadius);
        final double realMinMass = Math.max(MIN_MASS, minMass);

        // TODO(tobi): Cambiar brute force por CellIndexMethod
        int tries = 0;
        int particleCount = existingParticles.size();

        while(particleCount < targetParticleCount && tries < MAX_FAILURE_TOLERANCE) {
            final Particle2D particle = Particle2D.brownianRandomParticle(particleCount, spaceWidth, realMinMass, maxMass, minVelocity, maxVelocity, realMinRadius, maxRadius);
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
    public static class BrownianParticleGenerationConfig {
        public ParticleGenerationConfig littleParticles;
        public double  bigParticleRadius;
        public double  bigParticleMass;
        public double  bigParticleVelocity;
        public double  bigParticleXPosition;
        public double  bigParticleYPosition;
    }
}
