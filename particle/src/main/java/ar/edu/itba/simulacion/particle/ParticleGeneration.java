package ar.edu.itba.simulacion.particle;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ParticleGeneration {

    private ParticleGeneration() {
        // Static class
    }

    private static final double MIN_AXIS                = Double.MIN_VALUE;
    private static final double MIN_RADIUS              = 0;
    private static final double MIN_MASS                = 0;
    private static final int    MAX_FAILURE_TOLERANCE   = 10_000;

    public static void main(final String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        final ObjectMapper mapper = new ObjectMapper()
            // Hacemos esto por retrocompatibilidad y facilidad de uso
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            ;

        final List<ParticleGenerationConfig> configs = List.of(mapper.readValue(new File(args[0]), ParticleGenerationConfig[].class));
        if(configs.isEmpty()) {
            throw new IllegalArgumentException("No config found");
        }
        final List<String> outputFiles = configs.stream()
            .map(ParticleGenerationConfig::getOutputFile)
            .filter(Objects::nonNull).distinct()
            .collect(Collectors.toList())
            ;
        if(outputFiles.size() != 1) {
            throw new IllegalArgumentException("Multiple different output files found. All must be the same, or not be specified");
        }

        mapper.writeValue(new File(outputFiles.get(0)), particleGenerator(configs));
    }

    public static List<Particle2D> particleGenerator(final ParticleGenerationConfig config) {
        return particleGenerator(List.of(config));
    }

    public static List<Particle2D> particleGenerator(final List<ParticleGenerationConfig> configs) {
        final long totalParticleCount = configs.stream().mapToInt(ParticleGenerationConfig::getParticleCount).count();
        final List<Particle2D> particles = new ArrayList<>((int) totalParticleCount);

        for(final ParticleGenerationConfig config : configs) {
            generateAdditionalParticles(
                particles,          config.particleCount,
                config.spaceWidth,  config.periodicBorder,
                config.minX,        config.maxX,
                config.minY,        config.maxY,
                config.minVelocity, config.maxVelocity,
                config.minMass,     config.maxMass,
                config.minRadius,   config.maxRadius
            );
        }

        return particles;
    }

    public static void generateAdditionalParticles(
        final List<Particle2D> existingParticles,   final int targetParticleCount,
        final double spaceWidth,                    final boolean periodicBorder,
        final double minX,                          final double maxX,
        final double minY,                          final double maxY,
        final double minVelocity,                   final double maxVelocity,
        final double minMass,                       final double maxMass,
        final double minRadius,                     final double maxRadius
    ) {

        final double realMinRadius  = Math.max(MIN_RADIUS,  minRadius);
        final double realMinX       = Math.max(MIN_AXIS + realMinRadius,    minX);
        final double realMaxX       = Math.min(maxX,        spaceWidth - realMinRadius);
        final double realMinY       = Math.max(MIN_AXIS + realMinRadius,    minY);
        final double realMaxY       = Math.min(maxY,        spaceWidth - realMinRadius);
        final double realMinMass    = Math.max(MIN_MASS,    minMass);

        int tries = 0;
        int particleCount = existingParticles.size();
        while(particleCount < targetParticleCount && tries < MAX_FAILURE_TOLERANCE) {
            final Particle2D particle = Particle2D.randomParticle(
                particleCount,
                realMinX,       realMaxX,
                realMinY,       realMaxY,
                minVelocity,    maxVelocity,
                realMinMass,    maxMass,
                realMinRadius,  maxRadius
            );
            final double x = particle.getX();
            final double y = particle.getY();
            final double r = particle.getRadius();

            final boolean inBounds = periodicBorder || (
                x - r >= MIN_AXIS   &&
                x + r < spaceWidth  &&
                y - r >= MIN_AXIS   &&
                y + r < spaceWidth
            );
            if(inBounds && (particle.getRadius() == 0 || !particle.collides(existingParticles, spaceWidth, periodicBorder))) {
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
        public double  minX;
        @Builder.Default
        public double  maxX = Double.MAX_VALUE;
        public double  minY;
        @Builder.Default
        public double  maxY = Double.MAX_VALUE;
        public double  minVelocity;
        public double  maxVelocity;
        public double  minRadius;
        public double  maxRadius;
        public double  minMass;
        public double  maxMass;
        public String  outputFile;
    }
}
