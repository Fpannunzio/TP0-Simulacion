package ar.edu.itba.simulacion.tp3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.particle.Particle2D;
import ar.edu.itba.simulacion.particle.ParticleGeneration;
import ar.edu.itba.simulacion.particle.ParticleGeneration.ParticleGenerationConfig;
import ar.edu.itba.simulacion.tp3.BrownianParticleSystem.SimulationState;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public class Ej2 {
    public static void main( String[] args ) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        final ObjectMapper mapper = new ObjectMapper();

        final Ej2Config config = mapper.readValue(new File(args[0]), Ej2Config.class);

        final Ej2Summary summary = new Ej2Summary(new LinkedList<>());

        for(int particleCount : config.particleCounts) {

            System.out.printf("Working with %d particles.\n", particleCount);

            config.particleGenerationConfig.get(1).particleCount = particleCount;

            List<Ej2Round> rounds = new ArrayList<>(config.rounds);

            for (int i = 0; i < config.rounds; i++) {
                final List<Particle2D> initialState = ParticleGeneration.particleGenerator(config.getParticleGenerationConfig());

                if(initialState.isEmpty()) {
                    throw new IllegalArgumentException("No small particles for initial state found");
                }

                final BrownianParticleSystem brownianSystem = new BrownianParticleSystem(config.spaceWidth, initialState);
                brownianSystem.calculateNCollision(config.iterations);

                rounds.add(calculateRound(brownianSystem.getStates(), particleCount, config.iterations));
            }
            summary.getRoundsList().add(rounds);
        }

        mapper.writeValue(new File(config.outputFile), summary);

    }

    private static Ej2Round calculateRound(List<SimulationState> states, int particleCount, int iterations) {
        return Ej2Round.builder()
        .withParticleCount(particleCount)
        .withInitialVelocities(states.get(0).getParticles().stream().filter(Objects::nonNull).filter(p -> p.getId() != 0).map(Particle2D::getVelocityMod).collect(Collectors.toList()))
        .withLastThirdVelocities(getLastThirdVelocities(states, iterations, particleCount)).build();

    }

    private static List<Double> getLastThirdVelocities(List<SimulationState> states, int iterations, int particleCount) {
        List<Double> lastThirdVelocities = new ArrayList<>(Math.round(iterations/3) * particleCount);
        int currentIteration = 0;
        for (SimulationState state : states) {
            if (currentIteration > (2 * Math.round(iterations/3))){
                lastThirdVelocities.addAll(state.getParticles().stream().filter(Objects::nonNull).filter(p -> p.getId() != 0).map(Particle2D::getVelocityMod).collect(Collectors.toList()));
            }
            currentIteration++;
        }
        return lastThirdVelocities;
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class Ej2Config {
        public List<ParticleGenerationConfig>   particleGenerationConfig;
        public int[]                            particleCounts;
        public int                              rounds;
        public double                           spaceWidth;
        public int                              iterations;
        public String                           particlesFile;
        public String                           outputFile;
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class Ej2Summary {
        public List<List<Ej2Round>>     roundsList;
    }


    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class Ej2Round {
        public int                  particleCount;
        public List<Double>         lastThirdVelocities;
        public List<Double>         initialVelocities;
    }
}
