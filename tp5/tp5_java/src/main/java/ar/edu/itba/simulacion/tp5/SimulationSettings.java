package ar.edu.itba.simulacion.tp5;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import ar.edu.itba.simulacion.particle.Particle2D;
import ar.edu.itba.simulacion.particle.ParticleGeneration;
import ar.edu.itba.simulacion.particle.ParticleGeneration.ParticleGenerationConfig;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public final class SimulationSettings {
    private SimulationSettings() {
        // static
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class PedestrianDynamicsConfig {
        public final double tau;
        public final double beta;
        public final double exitLength;
        public final double minRadius;
        public final double maxRadius;
        public final double desiredVelocity;
        public final double escapeVelocity;

        @Builder.Default
        public       int   iterations = 100_000;
        @Builder.Default
        public       long   seed = ThreadLocalRandom.current().nextLong();

        public final ParticleGenerationConfig particleGeneration;
        public final String outputFile;

        public List<Particle2D> generateInitialState() {
            return ParticleGeneration.particleGenerator(particleGeneration);
        }

        public PedestrianDynamicsSimulation toSimulation() {
            return PedestrianDynamicsSimulation.builder()
                .withTau            (tau)
                .withBeta           (beta)
                .withExitLength     (exitLength)
                .withMinRadius      (minRadius)
                .withMaxRadius      (maxRadius)
                .withDesiredVelocity(desiredVelocity)
                .withEscapeVelocity (escapeVelocity)
                .withSpaceWidth     (particleGeneration.spaceWidth)
                .withRandomGen      (new Random(seed))
                .build()
                ;
        }
    }
}
