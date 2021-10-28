package ar.edu.itba.simulacion.tp5;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ar.edu.itba.simulacion.particle.Particle2D;
import ar.edu.itba.simulacion.particle.ParticleGeneration;
import ar.edu.itba.simulacion.particle.ParticleGeneration.ParticleGenerationConfig;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public class SimulationSettings {
    private SimulationSettings() {
        // static
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class PedestrianDynamicsConfig {
        public final double tau;
        public final double beta;
        public final double exitDistance;
        public final double minRadius;
        public final double maxRadius;
        public final double desiredVelocity;
        public final double escapeVelocity;
        @Builder.Default
        public Long   seed = ThreadLocalRandom.current().nextLong();

        public final ParticleGenerationConfig particleGeneration;
        public final String outputFile;

        public List<Particle2D> generateInitialState() {
            return ParticleGeneration.particleGenerator(particleGeneration);
        }

        public PedestrianDynamicsSimulation toSimulation() {
            return PedestrianDynamicsSimulation.builder()
                .withTau            (tau)
                .withBeta           (beta)
                .withExitLeft       (particleGeneration.spaceWidth/2 - exitDistance/2)
                .withExitRight      (particleGeneration.spaceWidth/2 + exitDistance/2)
                .withMinRadius      (minRadius)
                .withMaxRadius      (maxRadius)
                .withDesiredVelocity(desiredVelocity)
                .withEscapeVelocity (escapeVelocity)
                .withSpaceWidth     (particleGeneration.spaceWidth)
                .withSeed           (seed)
                .build()
                ;
        }
    }
}
